package com.example.investmenttradingservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Конфигурация кэширования с использованием Caffeine Cache
 * 
 * <p>
 * Этот класс настраивает систему кэширования для Investment Trading Service.
 * Используется библиотека Caffeine для высокопроизводительного кэширования в
 * памяти.
 * </p>
 * 
 * <p>
 * Основные особенности конфигурации:
 * </p>
 * <ul>
 * <li>Максимальный размер кэша: 10,000 записей</li>
 * <li>Время жизни данных: 24 часа после записи</li>
 * <li>Поддержка различных типов кэшей для разных данных</li>
 * </ul>
 * 
 * <p>
 * Типы кэшей:
 * </p>
 * <ul>
 * <li>sharesCache - кэш для акций</li>
 * <li>futuresCache - кэш для фьючерсов</li>
 * <li>indicativesCache - кэш для индикативных цен</li>
 * <li>closePricesCache - кэш для цен закрытия</li>
 * </ul>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Имя кэша для акций */
    private static final String SHARE_CACHE = "sharesCache";

    /** Имя кэша для фьючерсов */
    private static final String FUTURES_CACHE = "futuresCache";

    /** Имя кэша для индикативных цен */
    private static final String INDICATIVES_CACHE = "indicativesCache";

    /** Имя кэша для цен закрытия */
    private static final String CLOSE_PRICES_CACHE = "closePricesCache";

    private static final String OPEN_PRICES_CACHE = "openPricesCache";
    
    /** Имя кэша для цен закрытия вечерней сессии */
    private static final String CLOSE_PRICES_EVENING_SESSION_CACHE = "closePricesEveningSessionCache";

    /**Имя кэша для цен последних сделок по инструментам */
    private static final String LAST_PRICE_CACHE = "lastPricesCache";

    /**Имя кэша для дивидентных событий */
    private static final String DIVIDENDS_CACHE = "dividendsCache";

    /**Имя кэша для лимитов */
    private static final String LIMITS_CACHE = "limitsCache";


    /**
     * Создает конфигурацию Caffeine для кэширования
     * 
     * <p>
     * Настраивает основные параметры кэша:
     * </p>
     * <ul>
     * <li>Максимальный размер: 10,000 записей</li>
     * <li>Время жизни: 24 часа после записи</li>
     * </ul>
     * 
     * @return настроенный объект Caffeine для использования в CacheManager
     */
    @Bean
    public Caffeine<Object, Object> caffeineSpec() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofHours(24));
    }

    /**
     * Создает основной CacheManager для приложения
     * 
     * <p>
     * Настраивает CaffeineCacheManager с предопределенными кэшами для различных
     * типов данных.
     * Все кэши используют одинаковую конфигурацию Caffeine.
     * </p>
     * 
     * @param caffeine конфигурация Caffeine, созданная методом caffeineSpec()
     * @return настроенный CacheManager для использования в приложении
     */
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                SHARE_CACHE,
                FUTURES_CACHE,
                INDICATIVES_CACHE,
                CLOSE_PRICES_CACHE,
                OPEN_PRICES_CACHE,
                CLOSE_PRICES_EVENING_SESSION_CACHE,
                LAST_PRICE_CACHE, 
                DIVIDENDS_CACHE,
                LIMITS_CACHE);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

}