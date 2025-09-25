package com.lumibee.hive.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.sql.DataSource;

import com.lumibee.hive.filter.RedisSessionFilter;
import com.lumibee.hive.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;
import com.lumibee.hive.filter.JwtAuthenticationFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisSessionFilter redisSessionFilter;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        //第一次运行需要设为true,之后注释掉即可
        //tokenRepository.setCreateTableOnStartup(true);
        return tokenRepository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://www.hivelumi.com",
            "https://hivelumi.com", 
            "https://api.hivelumi.com",
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Qualifier("formLoginAuthenticationSuccessHandler") AuthenticationSuccessHandler formLoginSuccessHandler,
                                                   CustomOAuth2AuthenticationSuccessHandler customOAuth2SuccessHandler) throws Exception {

        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 允许所有OPTIONS请求通过
                        .requestMatchers("/", "/api/login", "/api/signup", "/css/**", "/js/**", "/img/**", "/favicon.ico",
                                        "/api/uploads/**", // 上传的文件（包括头像）
                                        "/api/home", // 首页 API
                                        "/api/tags/**", // 标签 API
                                        "/api/articles/**", // 文章 API
                                        "/api/article/**", // 单篇文章 API
                                        "/api/portfolios", // 作品集 API (GET)
                                        "/api/portfolio/**", // 作品集详情 API (GET)
                                        "/api/signup", // 注册API
                                        "/api/login", // API登录端点
                                        "/api/profile/**", // 个人资料 API
                                        "/api/login-process", // 登录处理URL
                                        "/api/user/current", // 获取当前用户信息API
                                        "/api/swagger-ui/**", // Swagger UI 界面
                                        "/api/swagger-ui.html", // Swagger UI 主页
                                        "/api/api-docs/**", // OpenAPI 文档
                                        "/api/v3/api-docs/**" // OpenAPI 3 文档
                                        ).permitAll() // 以上路径允许所有用户访问
                        .requestMatchers(HttpMethod.POST, "/api/portfolio").authenticated() // 创建作品集需要认证
                        .requestMatchers("/api/auth/refresh").permitAll() // Token刷新接口允许匿名访问
                        .requestMatchers("/api/publish", "/api/drafts", "/api/article/save-draft").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/article/*/comment").authenticated()
                        .requestMatchers("/api/ai/**").authenticated() // AI 相关 API 需要认证
                        .anyRequest().permitAll() // 其他所有请求允许匿名访问
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(redisSessionFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用CORS支持
                .exceptionHandling(exceptions -> 
                    exceptions.authenticationEntryPoint(customAuthenticationEntryPoint())
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/api/login") // 登录页的 GET 路径
                                .loginProcessingUrl("/api/login-process") // 登录表单 POST 提交到此路径
                                .usernameParameter("account") // 前端表单中用于输入邮箱或用户名的字段的 name 属性
                                .passwordParameter("password") // 前端表单中密码字段的 name 属性
                                .successHandler(formLoginSuccessHandler)
                                .failureHandler(customAuthenticationFailureHandler())
                                .permitAll()
                )
                .oauth2Login(oauth2 ->
                        oauth2
                                .loginPage("/api/login")
                                .successHandler(customOAuth2SuccessHandler)
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/api/logout") // 改为API登出端点
                                .logoutSuccessUrl("/") // 简化重定向URL
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID", "remember-me")
                                .clearAuthentication(true)
                                .permitAll()
                )
                .rememberMe(rememberMe ->
                        rememberMe
                                .tokenRepository(persistentTokenRepository())
                                .tokenValiditySeconds(1209600) // 2周 = 14 * 24 * 60 * 60
                                .userDetailsService(userServiceImpl)
                                .rememberMeParameter("remember-me")
                                .key("lumiHiveRememberMeKey")
                )
                .csrf(csrf -> csrf.disable()); // 完全禁用CSRF，因为这是纯API应用;


        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            String requestURI = request.getRequestURI();
            
            // 如果是 API 请求，返回 401 状态码和 JSON 响应
            if (requestURI.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Authentication Failed\",\"message\":\"认证失败，请检查用户名和密码\"}");
                return;
            }
            
            // 对于页面请求，根据环境动态选择重定向地址
            String host = request.getServerName();
            String redirectUrl;
            if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                redirectUrl = "http://localhost:3000/login?error=true";
            } else {
                redirectUrl = "https://www.hivelumi.com/login?error=true";
            }
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            String requestURI = request.getRequestURI();
            
            // 如果是 API 请求，返回 401 状态码和 JSON 响应
            if (requestURI.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"请先登录\"}");
                return;
            }
            
            // 对于页面请求，根据环境动态选择重定向地址
            String host = request.getServerName();
            String redirectUrl;
            if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                // 开发环境
                redirectUrl = "http://localhost:3000/login";
            } else {
                // 生产环境
                redirectUrl = "https://www.hivelumi.com/login";
            }
            response.sendRedirect(redirectUrl);
        };
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
                    user = userServiceImpl.selectByEmail(userIdentifier);
                } else {
                    user = userServiceImpl.selectByName(userIdentifier);
                }

                if (user != null) {
                    User sessionUser = new User();
                    sessionUser.setId(user.getId());
                    sessionUser.setName(user.getName());
                    sessionUser.setGithubId(user.getGithubId());
                    sessionUser.setQqOpenId(user.getQqOpenId());
                    sessionUser.setAvatarUrl(user.getAvatarUrl());
                    sessionUser.setEmail(user.getEmail());

                    session.setAttribute("user", sessionUser); //将 User 对象以键名 "user" 存入 session
                } else {
                    System.err.println("Form Login Success, but could not reload user from DB with identifier: " + userIdentifier);
                }
            } else {
                System.err.println("Form Login Success, but could not get user identifier from principal.");
            }

            // 使用HTTPS重定向
            String redirectUrl = request.getScheme().equals("https") ? 
                "https://" + request.getServerName() + "/" : 
                "/";
            response.sendRedirect(redirectUrl);
        };
    }

    @Component
    public static class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

        private final UserService userService;

        @Autowired
        public CustomOAuth2AuthenticationSuccessHandler(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
            HttpSession session = request.getSession();
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();


            String githubId = oauth2User.getName();
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            String avatarUrl = oauth2User.getAttribute("avatar_url");
            String bio = oauth2User.getAttribute("bio");

            User user = userService.selectByGithubId(githubId);
            boolean needsPasswordPrompt = false;

            if (user == null) {
                user = new User();
                user.setGithubId(githubId);
                user.setName(name);
                user.setEmail(email);
                user.setAvatarUrl(avatarUrl);
                user.setBio(bio);
                user.setGmtCreate(LocalDateTime.now());
                user.setGmtModified(user.getGmtCreate());
                user.setPassword(null); // 新 OAuth 用户，本地密码为 null
                userService.insert(user);
                needsPasswordPrompt = true;
            }  else {
                if (user.getPassword() == null || user.getPassword().isBlank()) {
                    needsPasswordPrompt = true;
                }

                user.setGmtModified(LocalDateTime.now());
                userService.updateById(user);
            }

            session.setAttribute("user", user);

            if (needsPasswordPrompt) {
                session.setAttribute("showPasswordSetupPrompt", true);
            } else {
                session.removeAttribute("showPasswordSetupPrompt");
            }
            // 使用HTTPS重定向
            String redirectUrl = request.getScheme().equals("https") ? 
                "https://" + request.getServerName() + request.getContextPath() + "/" : 
                request.getContextPath() + "/";
            response.sendRedirect(redirectUrl);
        }
    }
}