package com.lumibee.hive.config;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpConfig {

    @Value("${deepseek.api.key}")
    private String deepseekApiKey;

    @Bean
    public OkHttpClient okHttpClient() {

        // 创建连接池，设置最大空闲连接数和连接存活时间
        ConnectionPool connectionPool = new ConnectionPool(10, 2, TimeUnit.MINUTES);

        // 创建日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        // 缓存配置
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), "okhttp_cache");
        Cache cache = new Cache(cacheDir, 10 * 1024 * 1024);

        return new OkHttpClient.Builder()
                // 设置连接超时时间、读取超时时间、写入超时时间
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                // 设置连接池
                .connectionPool(connectionPool)
                // 设置缓存
                .cache(cache)
                // 添加拦截器
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new RetryInterceptor(3))
                .addInterceptor(new AuthInterceptor(deepseekApiKey))
                // 添加网络拦截器，用于缓存控制
                .addNetworkInterceptor(new CacheControlInterceptor())
                // 添加自定义请求头
                .addInterceptor(chain -> {
                    return chain.proceed(chain.request().newBuilder()
                            .addHeader("User-Agent", "Hive-Blog-System/1.0")
                            .build());
                })
                // 支持HTTP/2
                .protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2))
                // 自动重试连接失败的请求
                .retryOnConnectionFailure(true)
                .build();
    }


    // 重试拦截器
    public class RetryInterceptor implements Interceptor {
        private final int maxRetries;

        public RetryInterceptor(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException exception = null;

            for (int retry = 0; retry <= maxRetries; retry++) {
                try {
                    // 如果之前有响应，关闭它以释放资源
                    if (response != null) {
                        response.close();
                    }
                    // 发送请求
                    response = chain.proceed(request);
                    // 检查响应是否成功
                    if (response.isSuccessful() || response.code() < 500) {
                        return response;
                    }
                    if (retry < maxRetries) {
                        response.close();
                        response = null;
                    }
                } catch (IOException e) {
                    exception = e;
                    if (retry == maxRetries) {
                        throw e;
                    }
                }

                try {
                    Thread.sleep((long) (Math.pow(2, retry) * 1000)); //  指数退避
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    throw new IOException("Interrupted while waiting to retry", e);
                }
            }

            if (response != null) {
                response.close(); // 确保响应被关闭
            }
            // 如果所有重试都失败了，抛出最后一个异常
            throw exception;
        }
    }

    // 缓存控制拦截器
    public class CacheControlInterceptor implements Interceptor {

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            // 为特定接口添加缓存控制
            if (request.url().encodedPath().contains("/api/")) {
                return response.newBuilder()
                        .header("Cache-Control", "public, max-age=300") // 5分钟缓存
                        .build();
            }

            return response;
        }
    }

    public class AuthInterceptor implements Interceptor {
        private final String apiKey;

        public AuthInterceptor(String apiKey) {
            this.apiKey = apiKey;
        }

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request original = chain.request();

            if (original.url().host().contains("deepseek.com")) {
                Request.Builder builder = original.newBuilder()
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json");

                return chain.proceed(builder.build());
            }
            return chain.proceed(original);
        }
    }
}
