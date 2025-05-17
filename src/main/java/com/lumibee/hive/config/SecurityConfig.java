package com.lumibee.hive.config;

import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserMapper userMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Qualifier("formLoginAuthenticationSuccessHandler") AuthenticationSuccessHandler formLoginSuccessHandler,
                                                   CustomOAuth2AuthenticationSuccessHandler customOAuth2SuccessHandler) throws Exception {

        http
                .authorizeHttpRequests(authorizeRequests ->
                                authorizeRequests
                                        .requestMatchers(
                                                "/",              // 首页
                                                "/login",         // 登录页 (如果单独提供)
                                                "/signup",        // 注册页
                                                "/css/**",        // CSS 文件
                                                "/js/**",         // JavaScript 文件
                                                "/img/**",        // 图片文件
                                                "/error",         // Spring Boot 默认错误页
                                                "/favicon.ico",   // 网站图标
                                                "/api/user/dismiss-password-prompt"
                                        ).permitAll() // 以上路径允许所有用户访问
                                        .requestMatchers("/publish", "/api/ai/**").authenticated()
                                        .anyRequest().permitAll() // 其他所有未明确指定的请求也允许
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login") // 登录页的 GET 路径
                                .loginProcessingUrl("/login-process") // 登录表单 POST 提交到此路径
                                .usernameParameter("account") // 前端表单中用于输入邮箱或用户名的字段的 name 属性
                                .passwordParameter("password") // 前端表单中密码字段的 name 属性
                                .successHandler(formLoginAuthenticationSuccessHandler())
                                .failureUrl("/login?error=true")
                                .permitAll()
                )
                .oauth2Login(oauth2 ->
                        oauth2
                                .loginPage("/login")
                                .successHandler(customOAuth2SuccessHandler)
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/?logout")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID", "token")
                                .clearAuthentication(true)
                                .permitAll()
                );


        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler formLoginAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession();
            Object principal = authentication.getPrincipal();
            String userIdentifier = "";

            if (principal instanceof UserDetails) {
                userIdentifier = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                userIdentifier = (String) principal;
            }

            if (!userIdentifier.isEmpty()) {
                User user = null;
                if (userIdentifier.contains("@")) {
                    user = userMapper.selectByEmail(userIdentifier);
                } else {
                    user = userMapper.selectByName(userIdentifier);
                }

                if (user != null) {
                    User sessionUser = new User();
                    sessionUser.setId(user.getId());
                    sessionUser.setName(user.getName());
                    sessionUser.setAccountId(user.getAccountId());
                    sessionUser.setAvatarUrl(user.getAvatarUrl());
                    sessionUser.setEmail(user.getEmail());
                    sessionUser.setToken(user.getToken());

                    session.setAttribute("user", sessionUser); //将 User 对象以键名 "user" 存入 session
                    System.out.println("Form Login Success: User '" + sessionUser.getName() + "' set in session.");

                    if (sessionUser.getToken() != null) {
                        response.addCookie(new Cookie("token", sessionUser.getToken()));
                    }
                } else {
                    System.err.println("Form Login Success, but could not reload user from DB with identifier: " + userIdentifier);
                }
            } else {
                System.err.println("Form Login Success, but could not get user identifier from principal.");
            }
            response.sendRedirect("/");
        };
    }

    @Component
    class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

        private final UserMapper userMapper;

        @Autowired
        public CustomOAuth2AuthenticationSuccessHandler(UserMapper userMapper) {
            this.userMapper = userMapper;
        }

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
            HttpSession session = request.getSession();
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            String accountId = oauth2User.getName();
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            String avatarUrl = oauth2User.getAttribute("avatar_url");
            String bio = oauth2User.getAttribute("bio");

            User user = userMapper.selectByAccountId(accountId);
            boolean needsPasswordPrompt = false;

            if (user == null) {
                user = new User();
                user.setAccountId(accountId);
                user.setName(name);
                user.setEmail(email);
                user.setAvatarUrl(avatarUrl);
                user.setBio(bio);
                user.setGmtCreate(System.currentTimeMillis());
                user.setGmtModified(user.getGmtCreate());
                user.setPassword(null); // 新 OAuth 用户，本地密码为 null
                user.setToken(UUID.randomUUID().toString());
                userMapper.insert(user);
                needsPasswordPrompt = true;
                System.out.println("New user created via Spring Security OAuth2: " + user.getName());
            }  else {
                if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                    needsPasswordPrompt = true;
                }

                user.setGmtModified(System.currentTimeMillis());
                userMapper.updateById(user);
                System.out.println("Updated existing user info via Spring Security OAuth2: " + user.getName());
            }

            session.setAttribute("user", user);

            if (needsPasswordPrompt) {
                session.setAttribute("showPasswordSetupPrompt", true);
            } else {
                session.removeAttribute("showPasswordSetupPrompt");
            }

            if (user.getToken() != null) {
                response.addCookie(new Cookie("token", user.getToken()));
            }

            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}