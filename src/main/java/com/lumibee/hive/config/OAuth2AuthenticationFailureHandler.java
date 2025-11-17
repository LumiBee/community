package com.lumibee.hive.config;

import com.lumibee.hive.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository = new HttpCookieOAuth2AuthorizationRequestRepository();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        log.error("OAuth2 认证失败: {}", exception.getMessage());
        authRequestRepository.removeAuthorizationRequestCookies(request, response);
        // 简单重定向回登录页，携带错误信息
        String redirectUrl = isLocalhost(request.getServerName()) ?
                "http://localhost:3000/login?oauth_error=true" :
                "https://www.hivelumi.com/login?oauth_error=true";
        try {
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } catch (Exception ignored) {}
    }

    private boolean isLocalhost(String host) {
        return "localhost".equals(host) || "127.0.0.1".equals(host);
    }
}
