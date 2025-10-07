package com.example.investmenttradingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовый тест для проверки контекста Spring Boot приложения
 */
@SpringBootTest
@ActiveProfiles("test")
class InvestmentTradingServiceApplicationTests {

    @Test
    void contextLoads() {
        // Тест проверяет, что контекст Spring Boot загружается корректно
    }

}

