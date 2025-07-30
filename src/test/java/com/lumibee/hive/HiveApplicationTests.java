package com.lumibee.hive;

import com.lumibee.hive.service.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest
@ActiveProfiles("dev")
class HiveApplicationTests {

    @MockitoBean
    private ArticleRepository articleRepository;

    @Test
    void contextLoads() {
    }

}
