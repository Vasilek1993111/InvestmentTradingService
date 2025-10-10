package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Реализация менеджера кэша инструментов
 *
 * <p>
 * Предоставляет конкретную реализацию для работы с кэшем инструментов.
 * Инкапсулирует всю логику работы с Spring Cache и Caffeine Cache,
 * обеспечивая единообразный интерфейс для всех сервисов инструментов.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение данных из кэша по ключам</li>
 * <li>Работа с нативным интерфейсом Caffeine</li>
 * <li>Получение статистики по кэшам</li>
 * <li>Проверка существования кэшей</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class InstrumentCacheManagerImpl implements InstrumentCacheManager {

    /** Логгер для записи операций с кэшем */
    private static final Logger logger = LoggerFactory.getLogger(InstrumentCacheManagerImpl.class);

    /** Менеджер кэша Spring */
    private final CacheManager cacheManager;

    /**
     * Конструктор менеджера кэша инструментов
     *
     * @param cacheManager менеджер кэша Spring
     */
    public InstrumentCacheManagerImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Получает данные из кэша по имени кэша и возможным ключам
     *
     * @param <T>       тип данных в кэше
     * @param cacheName имя кэша
     * @param keys      возможные ключи для поиска
     * @return список данных из кэша или пустой список
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getFromCache(String cacheName, String[] keys) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warn("Кэш {} не найден", cacheName);
            return Collections.emptyList();
        }

        List<T> allData = new ArrayList<>();

        try {
            logger.debug("Поиск данных в кэше {} по ключам: {}",
                    cacheName, String.join(", ", keys));

            // Поиск по стандартным ключам
            for (String cacheKey : keys) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null && wrapper.get() instanceof List) {
                    List<T> data = (List<T>) wrapper.get();
                    if (data != null && !data.isEmpty()) {
                        allData.addAll(data);
                        logger.info("Найдено {} записей в кэше {} с ключом: {}",
                                data.size(), cacheName, cacheKey);
                        break; // Используем первый найденный ключ
                    }
                } else {
                    logger.debug("Ключ '{}' не найден в кэше {} или содержит неверный тип данных",
                            cacheKey, cacheName);
                }
            }

            if (allData.isEmpty()) {
                logger.warn("Данные не найдены в кэше {} ни по одному из ключей", cacheName);
            }

        } catch (Exception e) {
            logger.error("Ошибка получения данных из кэша {}: {}",
                    cacheName, e.getMessage(), e);
        }

        return allData;
    }

    /**
     * Получает данные из кэша через нативный интерфейс
     *
     * @param <T>       тип данных в кэше
     * @param cacheName имя кэша
     * @return список данных из кэша или пустой список
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getFromNativeCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warn("Кэш {} не найден", cacheName);
            return Collections.emptyList();
        }

        List<T> allData = new ArrayList<>();

        try {
            // Поиск через нативный интерфейс Caffeine
            if (cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                logger.debug("Поиск данных через нативный интерфейс Caffeine для кэша {}", cacheName);
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache
                        .getNativeCache();

                for (Map.Entry<?, ?> entry : caffeineCache.asMap().entrySet()) {
                    logger.debug("Проверка ключа в нативном кэше {}: {}", cacheName, entry.getKey());
                    if (entry.getValue() instanceof List) {
                        List<T> data = (List<T>) entry.getValue();
                        if (data != null && !data.isEmpty()) {
                            allData.addAll(data);
                            logger.info("Найдено {} записей в нативном кэше {} с ключом: {}",
                                    data.size(), cacheName, entry.getKey());
                            break; // Используем первый найденный список
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Ошибка получения данных через нативный интерфейс из кэша {}: {}",
                    cacheName, e.getMessage(), e);
        }

        return allData;
    }

    /**
     * Получает информацию о состоянии всех кэшей
     *
     * @return Map с информацией о кэшах
     */
    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            String[] cacheNames = cacheManager.getCacheNames().toArray(new String[0]);
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheStats = new HashMap<>();
                    cacheStats.put("exists", true);
                    cacheStats.put("size", getCacheSize(cacheName));

                    if (cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                        com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache
                                .getNativeCache();
                        cacheStats.put("estimatedSize", caffeineCache.estimatedSize());
                    }

                    statistics.put(cacheName, cacheStats);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики кэшей: {}", e.getMessage(), e);
        }

        return statistics;
    }

    /**
     * Проверяет существование кэша
     *
     * @param cacheName имя кэша
     * @return true если кэш существует
     */
    @Override
    public boolean cacheExists(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        return cache != null;
    }

    /**
     * Получает размер кэша
     *
     * @param cacheName имя кэша
     * @return размер кэша
     */
    @Override
    public int getCacheSize(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return 0;
        }

        try {
            if (cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache
                        .getNativeCache();
                return (int) caffeineCache.estimatedSize();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении размера кэша {}: {}", cacheName, e.getMessage(), e);
        }

        return 0;
    }
}
