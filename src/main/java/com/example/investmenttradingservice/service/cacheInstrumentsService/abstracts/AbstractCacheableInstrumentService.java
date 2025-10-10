package com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.CacheManager;

import com.example.investmenttradingservice.enums.InstrumentConfig;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.CacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

import jakarta.transaction.Transactional;

/**
 * Абстрактный базовый класс для сервисов кэшируемых инструментов
 *
 * <p>
 * Предоставляет общую функциональность для всех сервисов инструментов:
 * работа с кэшем, fallback на базу данных, поиск по FIGI.
 * Реализует принцип DRY (Don't Repeat Yourself) и обеспечивает
 * единообразное поведение всех сервисов инструментов.
 * </p>
 *
 * @param <T> тип DTO инструмента
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public abstract class AbstractCacheableInstrumentService<T> implements CacheableInstrumentService<T> {

    /** Логгер для записи операций с инструментами */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Менеджер кэша для работы с кэшированием */
    protected final CacheManager cacheManager;

    /** Менеджер кэша инструментов */
    protected final InstrumentCacheManager instrumentCacheManager;

    /** Тип инструмента для данного сервиса */
    protected final InstrumentType instrumentType;

    /**
     * Конструктор абстрактного сервиса
     *
     * @param cacheManager           менеджер кэша Spring
     * @param instrumentCacheManager менеджер кэша инструментов
     * @param instrumentType         тип инструмента
     */
    protected AbstractCacheableInstrumentService(
            CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            InstrumentType instrumentType) {
        this.cacheManager = cacheManager;
        this.instrumentCacheManager = instrumentCacheManager;
        this.instrumentType = instrumentType;
    }

    /**
     * Получает список всех инструментов из кэша с fallback на базу данных
     *
     * @return список инструментов в формате DTO
     */
    @Override
    @Transactional
    public List<T> getAll() {
        return getInstrumentData(false);
    }

    /**
     * Получает список всех инструментов ТОЛЬКО из кэша (без fallback на БД)
     *
     * @return список инструментов в формате DTO (может быть пустым)
     */
    @Override
    public List<T> getFromCacheOnly() {
        return getInstrumentData(true);
    }

    /**
     * Получает инструменты по FIGI из кэша с fallback на базу данных
     *
     * @param figi идентификатор инструмента
     * @return список найденных инструментов
     */
    @Override
    public List<T> getByFigi(String figi) {
        if (figi == null || figi.trim().isEmpty()) {
            logger.warn("FIGI не может быть пустым");
            return new ArrayList<>();
        }

        List<T> allData = getAll();
        return allData.stream()
                .filter(item -> figi.equals(getFigiFromItem(item)))
                .toList();
    }

    /**
     * Получает инструменты по FIGI ТОЛЬКО из кэша (без fallback на БД)
     *
     * @param figi идентификатор инструмента
     * @return список найденных инструментов из кэша
     */
    @Override
    public List<T> getByFigiFromCacheOnly(String figi) {
        if (figi == null || figi.trim().isEmpty()) {
            logger.warn("FIGI не может быть пустым");
            return new ArrayList<>();
        }

        List<T> allData = getFromCacheOnly();
        return allData.stream()
                .filter(item -> figi.equals(getFigiFromItem(item)))
                .toList();
    }

    /**
     * Получает количество инструментов в кэше
     *
     * @return количество инструментов
     */
    @Override
    public int getCacheSize() {
        return instrumentCacheManager.getCacheSize(instrumentType.getCacheName());
    }

    /**
     * Получает количество инструментов в базе данных
     *
     * @return количество инструментов в БД
     */
    @Override
    public int getDatabaseSize() {
        try {
            InstrumentConfig config = findConfigByType(instrumentType);
            if (config != null && config.getDbSupplier() != null) {
                Object dbData = config.getDbSupplier().get();
                if (dbData instanceof List) {
                    return ((List<?>) dbData).size();
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении размера БД для {}: {}",
                    instrumentType.getCacheName(), e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Универсальный метод для получения данных инструментов
     *
     * @param cacheOnly если true, не использует fallback на БД
     * @return список DTO данных
     */
    @SuppressWarnings("unchecked")
    protected List<T> getInstrumentData(boolean cacheOnly) {
        List<T> cachedData = getDataFromCache();

        if (cachedData != null && !cachedData.isEmpty()) {
            String cacheType = cacheOnly ? "только кэш" : "из кэша";
            logger.info("Получено {} записей {} для {}",
                    cachedData.size(), cacheType, instrumentType.getCacheName());
            return cachedData;
        }

        if (cacheOnly) {
            logger.info("Кэш {} пуст, возвращаем пустой список", instrumentType.getCacheName());
            return new ArrayList<>();
        }

        // Fallback на БД
        logger.info("Кэш {} пуст, загружаем из БД", instrumentType.getCacheName());

        InstrumentConfig config = findConfigByType(instrumentType);
        if (config != null && config.getDbSupplier() != null && config.getMapperFunction() != null) {
            try {
                Object dbData = config.getDbSupplier().get();
                Function<Object, List<T>> mapper = (Function<Object, List<T>>) config.getMapperFunction();
                return mapper.apply(dbData);
            } catch (Exception e) {
                logger.error("Ошибка при загрузке данных из БД для {}: {}",
                        instrumentType.getCacheName(), e.getMessage(), e);
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    /**
     * Получает данные из кэша
     *
     * @return список данных из кэша или пустой список
     */
    @SuppressWarnings("unchecked")
    protected List<T> getDataFromCache() {
        String[] possibleKeys = instrumentType.getPossibleKeys();
        List<T> data = instrumentCacheManager.getFromCache(instrumentType.getCacheName(), possibleKeys);

        if (data.isEmpty()) {
            data = instrumentCacheManager.getFromNativeCache(instrumentType.getCacheName());
        }

        return data != null ? data : Collections.emptyList();
    }

    /**
     * Извлекает FIGI из объекта инструмента
     * Должен быть реализован в каждом конкретном сервисе
     *
     * @param item объект инструмента
     * @return FIGI инструмента
     */
    protected abstract String getFigiFromItem(T item);

    /**
     * Находит конфигурацию по типу инструмента
     *
     * @param instrumentType тип инструмента
     * @return конфигурация или null
     */
    private InstrumentConfig findConfigByType(InstrumentType instrumentType) {
        for (InstrumentConfig config : InstrumentConfig.values()) {
            if (config.getInstrumentType() == instrumentType) {
                return config;
            }
        }
        return null;
    }
}
