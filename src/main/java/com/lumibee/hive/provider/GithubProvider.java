package com.lumibee.hive.provider;

import com.alibaba.fastjson2.JSON;
import com.lumibee.hive.dto.AccessTokenDTO;
import com.lumibee.hive.dto.GithubUser;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class GithubProvider {

    public String getAccessToken(AccessTokenDTO accessTokenDTO) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody postBody = RequestBody.create(mediaType, JSON.toJSONString(accessTokenDTO));

        Request request = new Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .post(postBody)
                .build();

        try(Response response = client.newCall(request).execute()) {
            String str = response.body().string();
            System.out.println(">>>>>>> In GithubProvider, getAccessToken(), str: " + str);
            return str.split("&")[0].split("=")[1];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GithubUser getUser(String accessToken) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String str = response.body().string();
            return JSON.parseObject(str, GithubUser.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
