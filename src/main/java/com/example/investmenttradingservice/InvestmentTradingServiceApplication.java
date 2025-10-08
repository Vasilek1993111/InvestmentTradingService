package com.example.investmenttradingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Главный класс приложения Investment Trading Service
 * 
 * <p>
 * Этот сервис предназначен для управления торговыми операциями и
 * инвестиционными портфелями.
 * Основные функции включают:
 * </p>
 * <ul>
 * <li>Интеграция с Tinkoff Invest API для получения рыночных данных</li>
 * <li>Кэширование инструментов (акции, фьючерсы) для повышения
 * производительности</li>
 * <li>Управление ценами закрытия и историческими данными</li>
 * <li>Предоставление REST API для клиентских приложений</li>
 * </ul>
 * 
 * <p>
 * Сервис использует следующие технологии:
 * </p>
 * <ul>
 * <li>Spring Boot для основного фреймворка</li>
 * <li>PostgreSQL для хранения данных</li>
 * <li>Caffeine для кэширования</li>
 * <li>gRPC для взаимодействия с Tinkoff Invest API</li>
 * </ul>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class InvestmentTradingServiceApplication {

    /**
     * Точка входа в приложение
     * 
     * <p>
     * Запускает Spring Boot приложение с автоматической конфигурацией.
     * Приложение будет доступно на порту, указанном в конфигурации (по умолчанию
     * 8080).
     * </p>
     * 
     * @param args аргументы командной строки, передаваемые в приложение
     */
    public static void main(String[] args) {
        SpringApplication.run(InvestmentTradingServiceApplication.class, args);
    }

}
