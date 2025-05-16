package com.lumibee.hive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;


import java.time.Duration;

@SpringBootApplication
public class HiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(HiveApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) { // 注入 RestTemplateBuilder

        return builder
                .build();
    }


}
