package com.lumibee.hive.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3 配置类
 * 用于生成 API 文档
 */
@Configuration
public class SwaggerConfig {

    /**
     * 定义安全方案名称常量
     */
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lumi Hive API")
                        .description("Lumi Hive 博客平台的 RESTful API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Lumi Hive Team")
                                .email("support@lumihive.com")
                                .url("https://lumihive.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("本地开发环境"),
                        new Server().url("https://api.lumihive.com").description("生产环境")
                ))
                // 添加全局安全要求
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // 添加安全方案
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, 
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("输入您的 JWT token")));
    }
}
