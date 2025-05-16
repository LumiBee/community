package com.lumibee.hive.config;

import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                                authorizeRequests
                                        .requestMatchers(
                                                "/",              // 首页
                                                "/login",         // 登录页 (如果单独提供)
                                                "/signup",        // 注册页
                                                "/callback",      // GitHub OAuth 回调
                                                "/css/**",        // CSS 文件
                                                "/js/**",         // JavaScript 文件
                                                "/img/**",        // 图片文件
                                                "/error",         // Spring Boot 默认错误页
                                                "/favicon.ico"
                                        ).permitAll() // 以上路径允许所有用户访问
                                        .requestMatchers("/publish", "/api/ai/**").authenticated() // 例如，发布和AI API需要登录
                                        .anyRequest().permitAll() // 其他所有未明确指定的请求也允许（如果您的应用大部分是公开的）
                        // 或者 .anyRequest().authenticated() 如果默认需要登录
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login") // 您的自定义登录页的 GET 路径
                                .loginProcessingUrl("/login-process") // 登录表单 POST 提交到此路径
                                .usernameParameter("account") // **重要**: 前端表单中用于输入邮箱或用户名的字段的 name 属性
                                .passwordParameter("password") // 前端表单中密码字段的 name 属性
                                .successHandler(formLoginAuthenticationSuccessHandler())
                                .failureUrl("/login?error=true")
                                .permitAll()
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/?logout")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID", "token") // 根据需要清除的 cookie
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
                userIdentifier = ((UserDetails) principal).getUsername(); // 这应该是邮箱或用户名 (取决于UserDetailsService的实现)
            } else if (principal instanceof String) {
                userIdentifier = (String) principal;
            }

            if (!userIdentifier.isEmpty()) {
                User user = null;
                if (userIdentifier.contains("@")) { // 假设 principal 是邮箱
                    user = userMapper.selectByEmail(userIdentifier);
                } else { // 假设 principal 是用户名
                    user = userMapper.selectByName(userIdentifier);
                }

                if (user != null) {
                    // 创建一个新的 User 对象或 DTO 用于 Session，避免暴露敏感信息或循环引用
                    User sessionUser = new User();
                    sessionUser.setId(user.getId());
                    sessionUser.setName(user.getName());
                    sessionUser.setAccountId(user.getAccountId());
                    sessionUser.setAvatarUrl(user.getAvatarUrl());
                    sessionUser.setEmail(user.getEmail());
                    sessionUser.setToken(user.getToken()); // 如果 token 是会话标识

                    session.setAttribute("user", sessionUser); // **关键：将 User 对象以键名 "user" 存入 session**
                    System.out.println("Form Login Success: User '" + sessionUser.getName() + "' set in session.");

                    // (可选) 如果您也需要设置名为 "token" 的 cookie
                    if (sessionUser.getToken() != null) {
                        response.addCookie(new Cookie("token", sessionUser.getToken()));
                    }
                } else {
                    System.err.println("Form Login Success, but could not reload user from DB with identifier: " + userIdentifier);
                }
            } else {
                System.err.println("Form Login Success, but could not get user identifier from principal.");
            }
            response.sendRedirect("/"); // 重定向到首页
        };
    }
}