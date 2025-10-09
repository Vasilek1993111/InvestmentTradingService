package com.example.investmenttradingservice.gRPCsevice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import lombok.Data;



@Configuration
@ConfigurationProperties(prefix = "tinvest")
@Data
public class TInvestConfiguration {

    @Value("${T_INVEST_TOKEN:}")
    private String token;

    // Основные параметры подключения
    private String target = "invest-public-api.tbank.ru:443";
    private String sandboxTarget = "sandbox-invest-public-api.tbank.ru:443";
    private boolean sandboxEnabled = false;

    // Настройки соединения
    private Duration connectionTimeout = Duration.ofSeconds(30);
    private Duration keepAliveTime = Duration.ofMinutes(1);
    private long maxMessageSize = 16 * 1024 * 1024; // 16MB

    // Retry настройки
    private int maxRetryAttempts = 3;
    private Duration retryDelay = Duration.ofSeconds(2);

    // Thread pool для асинхронных операций
    private int corePoolSize = 10;
    private int maxPoolSize = 50;
    private int queueCapacity = 1000;

    // Rate limiting
    private int requestsPerSecond = 100;
    private int burstCapacity = 200;


    
}