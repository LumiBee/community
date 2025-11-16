package com.lumibee.hive.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.lumibee.hive.filter.JwtAuthenticationFilter;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;
import com.lumibee.hive.service.UserServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private DataSource dataSource;
    
    
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
            "http://localhost:8090"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        // 明确指定允许的请求头，而不是使用通配符
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Accept", "Origin", "Access-Control-Request-Method", 
            "Access-Control-Request-Headers", "X-CSRF-TOKEN"
        ));
        // 明确指定暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Total-Count", 
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
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
                        // 1. 首先处理OPTIONS请求（CORS预检）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // 2. 静态资源和公开页面
                        .requestMatchers("/", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login", "/signup", "/home", "/articles", "/articles/**",
                                        "/portfolios", "/portfolios/**", "/profile/**", "/about",
                                        "/tags", "/tags/**").permitAll() // 前端页面路由

                        // 3. 认证相关接口（登录、注册、登出）
                        .requestMatchers("/api/login", "/api/signup", "/api/login-process", "/api/auth/refresh").permitAll()
                        
                        // 4. 公开的API接口（只读操作）
                        .requestMatchers(HttpMethod.GET, "/api/home").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tags/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/article/**").permitAll() // 获取文章详情
                        .requestMatchers(HttpMethod.GET, "/api/portfolios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/portfolio/**").permitAll() // 获取作品集详情
                        .requestMatchers(HttpMethod.GET, "/api/profile/**").permitAll() // 获取用户资料（查看）
                        .requestMatchers(HttpMethod.GET, "/api/user/current").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll() // 获取评论
                        .requestMatchers("/uploads/**").permitAll() // 上传的文件（包括头像）- 注意：这是静态资源路径，不是API路径
                        .requestMatchers(HttpMethod.GET, "/api/uploads/**").permitAll() // API方式访问上传文件
                        
                        // 5. Swagger文档（开发环境可考虑限制）
                        .requestMatchers("/api/swagger-ui/**", "/api/swagger-ui.html", 
                                        "/api/api-docs/**", "/api/v3/api-docs/**").permitAll()
                        
                        // 6. 需要认证的写操作 - 文章相关
                        .requestMatchers("/api/article/publish").authenticated() // 发布文章
                        .requestMatchers(HttpMethod.PUT, "/api/article/*/edit").authenticated() // 编辑文章（使用*匹配单个路径段）
                        .requestMatchers(HttpMethod.DELETE, "/api/article/delete/*").authenticated() // 删除文章（使用*匹配单个路径段）
                        .requestMatchers("/api/article/save-draft").authenticated() // 保存草稿
                        .requestMatchers("/api/article/drafts").authenticated() // 获取草稿列表
                        .requestMatchers(HttpMethod.POST, "/api/article/*/like").authenticated() // 点赞文章（使用*匹配单个路径段）
                        
                        // 7. 需要认证的写操作 - 作品集相关
                        .requestMatchers(HttpMethod.POST, "/api/portfolio").authenticated() // 创建作品集
                        .requestMatchers(HttpMethod.PUT, "/api/portfolio/**").authenticated() // 更新作品集
                        .requestMatchers(HttpMethod.DELETE, "/api/portfolio/**").authenticated() // 删除作品集
                        
                        // 8. 需要认证的写操作 - 用户资料相关
                        .requestMatchers(HttpMethod.PUT, "/api/profile/**").authenticated() // 更新用户资料
                        .requestMatchers(HttpMethod.POST, "/api/profile/**").authenticated() // 用户资料相关操作
                        
                        // 9. 需要认证的操作 - 收藏夹相关
                        .requestMatchers("/api/favorites/**").authenticated()
                        
                        // 10. 需要认证的操作 - 评论相关（必须在GET规则之后，但更具体的DELETE规则优先）
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/*").authenticated() // 删除评论（使用*匹配单个路径段，如 /api/comments/4）
                        .requestMatchers(HttpMethod.POST, "/api/comments/**").authenticated() // 添加评论
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").authenticated() // 更新评论
                        
                        // 11. 需要认证的操作 - AI相关
                        .requestMatchers("/api/ai/**").authenticated()
                        
                        // 12. 其他请求默认需要认证（更安全的做法）
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
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
                .oauth2Client(Customizer.withDefaults())
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
                .csrf(csrf -> csrf.disable()); // 完全禁用CSRF;


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