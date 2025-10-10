package com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces;

import java.util.List;

/**
 * Базовый интерфейс для сервисов кэшируемых инструментов
 *
 * <p>
 * Определяет контракт для работы с кэшируемыми инструментами.
 * Все сервисы инструментов должны реализовывать этот интерфейс
 * для обеспечения единообразного API.
 * </p>
 *
 * @param <T> тип DTO инструмента
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public interface CacheableInstrumentService<T> {

    /**
     * Получает список всех инструментов из кэша с fallback на базу данных
     *
     * @return список инструментов в формате DTO
     */
    List<T> getAll();

    /**
     * Получает список всех инструментов ТОЛЬКО из кэша (без fallback на БД)
     *
     * @return список инструментов в формате DTO (может быть пустым)
     */
    List<T> getFromCacheOnly();

    /**
     * Получает инструменты по FIGI из кэша с fallback на базу данных
     *
     * @param figi идентификатор инструмента
     * @return список найденных инструментов
     */
    List<T> getByFigi(String figi);

    /**
     * Получает инструменты по FIGI ТОЛЬКО из кэша (без fallback на БД)
     *
     * @param figi идентификатор инструмента
     * @return список найденных инструментов из кэша
     */
    List<T> getByFigiFromCacheOnly(String figi);

    /**
     * Получает количество инструментов в кэше
     *
     * @return количество инструментов
     */
    int getCacheSize();

    /**
     * Получает количество инструментов в базе данных
     *
     * @return количество инструментов в БД
     */
    int getDatabaseSize();
}
