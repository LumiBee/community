package com.lumibee.hive.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OkHttpService{

    private OkHttpClient client;
    private ObjectMapper objectMapper;

    public OkHttpService(OkHttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    // 同步 GET 请求
    public <T> T get(String url, Class<T> responseType) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " + response.message());
            }

            String json = response.body().string();
            return objectMapper.readValue(json, responseType);
        }
    }

    // 同步 POST 请求
    public <T, R> R post(String url, T requestBody, Class<R> responseType) throws IOException {
        return post(url, requestBody, responseType, null);
    }

    // 同步 POST 请求（带请求头）
    public <T, R> R post(String url, T requestBody, Class<R> responseType, Headers headers) throws IOException {
        String json = objectMapper.writeValueAsString(requestBody);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        
        if (headers != null) {
            requestBuilder.headers(headers);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " + response.message());
            }

            String responseJson = response.body().string();
            return objectMapper.readValue(responseJson, responseType);
        }
    }

    // 异步 POST 请求
    public <T, R> CompletableFuture<R> postAsync(String url, T requestBody, Class<R> responseType) {
        CompletableFuture<R> future = new CompletableFuture<> ();

        try {
            String json = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            future.completeExceptionally(
                                    new IOException("HTTP " + response.code() + ": " + response.message())
                            );
                            return;
                        }

                        String responseJson = response.body().string();
                        R result = objectMapper.readValue(responseJson, responseType);
                        future.complete(result);
                    } catch (IOException e) {
                        future.completeExceptionally(e);
                    }finally {
                        response.close();
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    future.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    // 异步批量请求
    public <T> CompletableFuture<List<T>> batchRequestAsync(List<String> urls, Class<T> responseType) {
        List<CompletableFuture<T>> futures = urls.stream()
                .map(url -> {
                    CompletableFuture<T> future = new CompletableFuture<>();

                    Request request = new Request.Builder().url(url).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            try {
                                if (!response.isSuccessful()) {
                                    String json = response.body().string();
                                    T result = objectMapper.readValue(json, responseType);
                                    future.complete(result);
                                } else {
                                    future.completeExceptionally(new IOException("HTTP " + response.code() + ": " + response.message()));
                                }
                            } catch (Exception e) {
                                future.completeExceptionally(e);
                            } finally {
                                response.close();
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            future.completeExceptionally(e);
                        }
                    });

                    return future;
                })
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
