package com.lumibee.hive.config;

import java.util.Arrays;

 
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.lumibee.hive.filter.JwtAuthenticationFilter;
import com.lumibee.hive.service.UserServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@Log4j2
public class SecurityConfig {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
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
        // 明确指定允许的请求头
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("=== 初始化 SecurityFilterChain ===");

        http
                .authorizeHttpRequests(authz -> {
                    log.info("配置授权规则...");
                    authz
                        // 1. 首先处理OPTIONS请求（CORS预检）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // 2. 静态资源和公开页面
                        .requestMatchers("/", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login", "/signup", "/home", "/articles", "/articles/**",
                                        "/portfolios", "/portfolios/**", "/profile/**", "/about",
                                        "/tags", "/tags/**").permitAll() // 前端页面路由

                        // 3. 认证相关接口（登录、注册、登出）
                        .requestMatchers("/login", "/login/oauth2", "/signup", "/auth/refresh").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll() // OAuth2回调接口
                        .requestMatchers("/error").permitAll() // 允许访问错误页面

                        // 4. 公开的API接口（只读操作）
                        .requestMatchers(HttpMethod.GET, "/home").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tags/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/articles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/article/**").permitAll() // 获取文章详情
                        .requestMatchers(HttpMethod.GET, "portfolios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/portfolio/**").permitAll() // 获取作品集详情
                        .requestMatchers(HttpMethod.GET, "/profile/**").permitAll() // 获取用户资料（查看）
                        .requestMatchers(HttpMethod.GET, "/user/current").permitAll()
                        .requestMatchers(HttpMethod.GET, "/comments/**").permitAll() // 获取评论
                        .requestMatchers("/uploads/**").permitAll() // 上传的文件（包括头像）- 注意：这是静态资源路径，不是API路径
                        .requestMatchers(HttpMethod.GET, "/api/uploads/**").permitAll() // API方式访问上传文件
                        
                        // 5. Swagger文档
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                        "/api-docs/**", "/v3/api-docs/**").permitAll()
                        
                        // 6. 需要认证的写操作 - 文章相关
                        .requestMatchers("/article/publish").authenticated() // 发布文章
                        .requestMatchers(HttpMethod.PUT, "/article/*/edit").authenticated() // 编辑文章（使用*匹配单个路径段）
                        .requestMatchers(HttpMethod.DELETE, "/article/delete/*").authenticated() // 删除文章（使用*匹配单个路径段）
                        .requestMatchers("/article/save-draft").authenticated() // 保存草稿
                        .requestMatchers("/articles/drafts").authenticated() // 获取草稿列表
                        .requestMatchers(HttpMethod.POST, "/article/*/like").authenticated() // 点赞文章（使用*匹配单个路径段）
                        
                        // 7. 需要认证的写操作 - 作品集相关
                        .requestMatchers(HttpMethod.POST, "/portfolio").authenticated() // 创建作品集
                        .requestMatchers(HttpMethod.PUT, "/portfolio/**").authenticated() // 更新作品集
                        .requestMatchers(HttpMethod.DELETE, "/portfolio/**").authenticated() // 删除作品集
                        
                        // 8. 需要认证的写操作 - 用户资料相关
                        .requestMatchers(HttpMethod.PUT, "/profile/**").authenticated() // 更新用户资料
                        .requestMatchers(HttpMethod.POST, "/profile/**").authenticated() // 用户资料相关操作
                        
                        // 9. 需要认证的操作 - 收藏夹相关
                        .requestMatchers("/favorites/**").authenticated()
                        
                        // 10. 需要认证的操作 - 评论相关（必须在GET规则之后，但更具体的DELETE规则优先）
                        .requestMatchers(HttpMethod.DELETE, "/comments/*").authenticated() // 删除评论（使用*匹配单个路径段，如 /comments/4）
                        .requestMatchers(HttpMethod.POST, "/comments/**").authenticated() // 添加评论
                        .requestMatchers(HttpMethod.PUT, "/comments/**").authenticated() // 更新评论
                        
                        // 11. 需要认证的操作 - AI相关
                        .requestMatchers("/ai/**").authenticated()
                        
                        // 12. 其他请求默认需要认证（更安全的做法）
                        .anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用CORS支持
                .exceptionHandling(exceptions -> 
                    exceptions.authenticationEntryPoint(customAuthenticationEntryPoint())
                )
                .oauth2Login(oauth2 -> {
                    log.info("配置OAuth2登录(无状态)...");
                    oauth2
                        .authorizationEndpoint(config ->
                                config.authorizationRequestRepository(new com.lumibee.hive.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository())
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler);
                })
                .logout(logout -> logout.permitAll())
                .csrf(csrf -> csrf.disable()); // 完全禁用CSRF;

        log.info("=== SecurityFilterChain 配置完成 ===");
        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            String requestURI = request.getRequestURI();
            log.error("=== 认证失败 - AuthenticationEntryPoint 被调用 ===");
            log.error("请求URI: {}", requestURI);
            log.error("请求方法: {}", request.getMethod());
            log.error("异常信息: {}", authException.getMessage());
            log.error("异常类型: {}", authException.getClass().getName());

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
}
