package com.lumibee.hive.config;

import com.itextpdf.io.exceptions.IOException;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;
import com.lumibee.hive.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Log4j2
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException, java.io.IOException {
        log.info("=== OAuth2认证成功处理器被调用 ===");
        log.info("请求URL: {}", request.getRequestURL());
        log.info("请求URI: {}", request.getRequestURI());
        log.info("请求参数: {}", request.getQueryString());

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        log.info("OAuth2User Principal: {}", oauth2User);
        log.info("OAuth2User Attributes: {}", oauth2User.getAttributes());

        String githubId = oauth2User.getName();
        String name = oauth2User.getAttribute("name");
        String email = oauth2User.getAttribute("email");
        String avatarUrl = oauth2User.getAttribute("avatar_url");
        String bio = oauth2User.getAttribute("bio");

        log.info("OAuth2登录成功，GitHub ID: {}, Name: {}, Email: {}, Avatar URL: {}, bio: {}", githubId, name, email, avatarUrl, bio);

        log.info("开始查找或创建用户...");
        User user = userService.findOrCreateUser(githubId, name, email, avatarUrl);
        log.info("用户处理完成，用户ID: {}, 用户名: {}", user.getId(), user.getName());

        log.info("开始生成JWT token...");
        String token = jwtUtil.generateToken(user.getId(), user.getName());
        log.info("JWT token生成成功: {}", token.substring(0, Math.min(20, token.length())) + "...");

        // 设置 HttpOnly Cookie 以便前端可直接依赖 Cookie 访问
        jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        long expiresInSec = Math.max(0L, (jwtUtil.getExpirationDateFromToken(token).getTime() - System.currentTimeMillis()) / 1000);
        jwtCookie.setMaxAge((int) Math.min(Integer.MAX_VALUE, expiresInSec));
        response.addCookie(jwtCookie);

        // 同时通过URL参数传给前端（可选）
        String redirectUrl = isLocalhost(request.getServerName()) ?
                "http://localhost:3000/callback?token=" + token :
                "https://www.hivelumi.com/callback?token=" + token;
        log.info("准备重定向到: {}", redirectUrl);
        try {
            new com.lumibee.hive.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository()
                    .removeAuthorizationRequestCookies(request, response);
        } catch (Exception ignore) {}
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        log.info("=== OAuth2认证成功处理完成 ===");
    }

    private boolean isLocalhost(String host) {
        return "localhost".equals(host) || "127.0.0.1".equals(host);
    }
}
