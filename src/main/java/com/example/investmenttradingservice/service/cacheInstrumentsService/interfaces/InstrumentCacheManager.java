package com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для управления кэшем инструментов
 *
 * <p>
 * Определяет контракт для работы с кэшем различных типов инструментов.
 * Обеспечивает абстракцию над механизмом кэширования и позволяет
 * легко заменить реализацию кэша в будущем.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public interface InstrumentCacheManager {

    /**
     * Получает данные из кэша по имени кэша и возможным ключам
     *
     * @param <T>       тип данных в кэше
     * @param cacheName имя кэша
     * @param keys      возможные ключи для поиска
     * @return список данных из кэша или пустой список
     */
    <T> List<T> getFromCache(String cacheName, String[] keys);

    /**
     * Получает данные из кэша через нативный интерфейс
     *
     * @param <T>       тип данных в кэше
     * @param cacheName имя кэша
     * @return список данных из кэша или пустой список
     */
    <T> List<T> getFromNativeCache(String cacheName);

    /**
     * Получает информацию о состоянии всех кэшей
     *
     * @return Map с информацией о кэшах
     */
    Map<String, Object> getCacheStatistics();

    /**
     * Проверяет существование кэша
     *
     * @param cacheName имя кэша
     * @return true если кэш существует
     */
    boolean cacheExists(String cacheName);

    /**
     * Получает размер кэша
     *
     * @param cacheName имя кэша
     * @return размер кэша
     */
    int getCacheSize(String cacheName);
}
