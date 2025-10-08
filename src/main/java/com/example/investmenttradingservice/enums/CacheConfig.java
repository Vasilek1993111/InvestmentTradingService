package com.example.investmenttradingservice.enums;

import java.util.function.Function;


/**
 * Enum для конфигурации кэшей в системе
 *
 * <p>
 * Определяет полную конфигурацию для каждого кэша:
 * - имя кэша
 * - ключ для сохранения данных
 * - функция для преобразования Entity в DTO
 * - описание для логирования
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public enum CacheConfig {

    SHARES("sharesCache", "all_shares", "акций", null),
    FUTURES("futuresCache", "all_futures", "фьючерсов", null),
    INDICATIVES("indicativesCache", "all_indicatives", "индикативов", null),
    CLOSE_PRICES("closePricesCache", "all_close_prices", "цен закрытия", null),
    OPEN_PRICES("openPricesCache", "all_open_prices", "цен открытия", null),
    CLOSE_PRICES_EVENING_SESSION("closePricesEveningSessionCache", "all_close_prices_evening_session", "цен закрытия вечерней сессии", null);

    /** Имя кэша */ 
    private final String cacheName;

    /** Ключ для сохранения данных в кэше */
    private final String cacheKey;

    /** Описание для логирования */
    private final String description;

    /** Функция для преобразования Entity в DTO (будет установлена через setter) */
    private Function<?, ?> mapperFunction;

    /**
     * Конструктор для конфигурации кэша
     *
     * @param cacheName      имя кэша
     * @param cacheKey       ключ для сохранения данных
     * @param description    описание для логирования
     * @param mapperFunction функция для преобразования Entity в DTO
     */
    CacheConfig(String cacheName, String cacheKey, String description, Function<?, ?> mapperFunction) {
        this.cacheName = cacheName;
        this.cacheKey = cacheKey;
        this.description = description;
        this.mapperFunction = mapperFunction;
    }

    /**
     * Получает имя кэша
     *
     * @return имя кэша
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Получает ключ для сохранения данных в кэше
     *
     * @return ключ кэша
     */
    public String getCacheKey() {
        return cacheKey;
    }

    /**
     * Получает описание для логирования
     *
     * @return описание
     */
    public String getDescription() {
        return description;
    }

    /**
     * Получает функцию для преобразования Entity в DTO
     *
     * @return функция преобразования
     */
    public Function<?, ?> getMapperFunction() {
        return mapperFunction;
    }

    /**
     * Устанавливает функцию для преобразования Entity в DTO
     *
     * @param mapperFunction функция преобразования
     */
    public void setMapperFunction(Function<?, ?> mapperFunction) {
        this.mapperFunction = mapperFunction;
    }
}
