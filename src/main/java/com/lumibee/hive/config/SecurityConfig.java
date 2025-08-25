package com.lumibee.hive.config;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.sql.DataSource;

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

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.CustomUserServiceImpl;
import com.lumibee.hive.service.UserService;
import com.lumibee.hive.service.UserServiceImpl;

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
    private CustomUserServiceImpl customUserServiceImpl;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Qualifier("formLoginAuthenticationSuccessHandler") AuthenticationSuccessHandler formLoginSuccessHandler,
                                                   CustomOAuth2AuthenticationSuccessHandler customOAuth2SuccessHandler) throws Exception {

        http
                .authorizeHttpRequests(authorizeRequests ->
                                authorizeRequests
                                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 允许所有OPTIONS请求通过
                                        .requestMatchers(
                                                "/",              // 首页
                                                "/login",         // 登录页 (如果单独提供)
                                                "/signup",        // 注册页
                                                "/sitemap.xml",  // 网站地图
                                                "/css/**",        // CSS 文件
                                                "/js/**",         // JavaScript 文件
                                                "/img/**",        // 图片文件
                                                "/error",         // Spring Boot 默认错误页
                                                "/favicon.ico",   // 网站图标
                                                "/api/user/dismiss-password-prompt",
                                                "/article/**",    // 文章浏览
                                                "/.well-known/**", // WebFinger 协议
                                                "/uploads/**",
                                                "/baidu_verify_codeva-wzTpwijk24.html", // 百度站长验证文件
                                                "/tags/**", // 标签浏览
                                                "/api/tags/**", // 标签 API
                                                "/portfolio/**", // 个人作品集
                                                "/api/portfolio/**", // 个人作品集 API
                                                "/api/portfolios", // 作品集列表 API
                                                "/avatars/**", // 头像图片
                                                "/backgrounds/**", // 背景图片
                                                "/search", // 搜索页面
                                                "/api/search/**", // 搜索 API
                                                "/api/home", // 首页数据 API
                                                "/api/articles/**", // 文章相关 API
                                                "/api/article/**", // 单篇文章 API
                                                "/api/profile/**", // 个人资料 API
                                                                                        "/api/user/current", // 获取当前用户 API
                                        "/api/signup", // 注册API
                                        "/api/login", // API登录端点
                                        "/api/ai", // AI 相关 API
                                        "/api/debug/**" // 调试API
                                        ).permitAll() // 以上路径允许所有用户访问
                                        .requestMatchers("/publish", "/api/ai/**","/user/settings","/drafts","/api/article/save-draft").authenticated()
                                        .requestMatchers(HttpMethod.POST, "/api/article/*/comment").authenticated()
                                        .anyRequest().authenticated() // 其他所有未明确指定的请求不允许匿名访问
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login") // 登录页的 GET 路径
                                .loginProcessingUrl("/login-process") // 登录表单 POST 提交到此路径
                                .usernameParameter("account") // 前端表单中用于输入邮箱或用户名的字段的 name 属性
                                .passwordParameter("password") // 前端表单中密码字段的 name 属性
                                .successHandler(formLoginSuccessHandler)
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
                )
                // 暂时禁用remember-me功能，避免token不匹配问题
                // .rememberMe(rememberMe ->
                //         rememberMe
                //                 .tokenRepository(persistentTokenRepository())
                //                 .tokenValiditySeconds(604800)
                //                 .userDetailsService(customUserServiceImpl)
                //                 .rememberMeParameter("remember-me")
                // )
                .csrf(csrf ->
                        csrf
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                .ignoringRequestMatchers(
                                        "/login-process", // 登录处理URL
                                        "/api/login", // API登录端点
                                        "/api/logout", // API登出端点
                                        "/api/search/**", // 搜索 API
                                        "/api/home", // 首页 API
                                        "/api/tags/**", // 标签 API
                                        "/api/articles/**", // 文章 API
                                        "/api/article/**", // 单篇文章 API
                                        "/api/portfolios", // 作品集 API
                                        "/api/user/current", // 用户信息 API
                                        "/api/signup", // 注册 API
                                        "/api/profile/**", // 个人资料 API
                                        "/api/debug/**" // 调试API
                                ) // API路径忽略CSRF
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
                    System.out.println("Form Login Success: User '" + sessionUser.getName() + "' set in session.");
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
                System.out.println("New user created via Spring Security OAuth2: " + user.getName());
            }  else {
                if (user.getPassword() == null || user.getPassword().isBlank()) {
                    needsPasswordPrompt = true;
                }

                user.setGmtModified(LocalDateTime.now());
                userService.updateById(user);
                System.out.println("Updated existing user info via Spring Security OAuth2: " + user.getName());
            }

            session.setAttribute("user", user);

            if (needsPasswordPrompt) {
                session.setAttribute("showPasswordSetupPrompt", true);
            } else {
                session.removeAttribute("showPasswordSetupPrompt");
            }
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}