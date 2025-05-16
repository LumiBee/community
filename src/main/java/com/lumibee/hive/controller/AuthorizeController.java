package com.lumibee.hive.controller;

import com.lumibee.hive.dto.AccessTokenDTO;
import com.lumibee.hive.dto.GithubUser;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.User;
import com.lumibee.hive.provider.GithubProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * AuthorizeController handles the authorization callback from the OAuth provider.
 * It processes the authorization code and state parameters returned by the provider.
 * created by lumibee on 2025/05/14
 */
@Controller
public class AuthorizeController {

    @Autowired
    private GithubProvider githubProvider;

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.client.redirect-uri}")
    private String redirectUri;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletResponse response) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(state);
        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        System.out.println(">>>>>> In AuthorizeController, githubUser.getAvatar_url(): " + (githubUser != null ? githubUser.getAvatar_url() : "githubUser is null"));
        if (githubUser != null) {
            // 登录成功
            // 将用户信息存储到数据库
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            user.setAvatarUrl(githubUser.getAvatar_url());
            userMapper.insert(user);
            response.addCookie(new Cookie("token", token));
            return "redirect:/";
        } else {
            // 登录失败
            return "redirect:/";
        }
    }
}
