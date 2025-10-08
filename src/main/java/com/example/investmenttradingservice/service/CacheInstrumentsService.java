package com.example.investmenttradingservice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.FutureDTO;
import com.example.investmenttradingservice.DTO.ShareDTO;
import com.example.investmenttradingservice.DTO.IndicativeDTO;
import com.example.investmenttradingservice.DTO.OpenPriceDTO;
import com.example.investmenttradingservice.mapper.Mapper;
import com.example.investmenttradingservice.entity.ClosePriceEntity;
import com.example.investmenttradingservice.entity.ClosePriceEveningSessionEntity;
import com.example.investmenttradingservice.entity.FutureEntity;
import com.example.investmenttradingservice.entity.IndicativeEntity;
import com.example.investmenttradingservice.entity.ShareEntity;
import com.example.investmenttradingservice.entity.OpenPriceEntity;
import com.example.investmenttradingservice.repository.ClosePriceEveningSessionRepository;
import com.example.investmenttradingservice.repository.ClosePriceRepository;
import com.example.investmenttradingservice.repository.FutureRepository;
import com.example.investmenttradingservice.repository.Indicativerepository;
import com.example.investmenttradingservice.repository.OpenPriceRepositrory;
import com.example.investmenttradingservice.repository.ShareRepository;
import com.example.investmenttradingservice.util.WorkingDaysUtils;
import com.example.investmenttradingservice.DTO.ClosePriceDTO;
import com.example.investmenttradingservice.DTO.ClosePriceEveningSessionDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.enums.InstrumentConfig;
import java.util.function.Function;

import jakarta.transaction.Transactional;

/**
 * Сервис для работы с кэшированными инструментами
 *
 * <p>
 * Этот сервис предоставляет методы для получения акций и фьючерсов из кэша
 * с автоматическим fallback на базу данных в случае отсутствия данных в кэше.
 * Обеспечивает высокую производительность за счет кэширования часто
 * используемых данных.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение акций из кэша с fallback на БД</li>
 * <li>Получение фьючерсов из кэша с fallback на БД</li>
 * <li>Получение индикативов из кэша с fallback на БД</li>
 * <li>Получение цен закрытия из кэша с fallback на БД</li>
 * <li>Получение цен открытия из кэша с fallback на БД</li>
 * <li>Автоматическое преобразование Entity в DTO</li>
 * <li>Обработка ошибок кэширования</li>
 * <li>Логирование операций для мониторинга</li>
 * </ul>
 *
 * <p>
 * Используемые кэши:
 * </p>
 * <ul>
 * <li>sharesCache - для акций</li>
 * <li>futuresCache - для фьючерсов</li>
 * <li>indicativesCache - для индикативов</li>
 * <li>closePricesCache - для цен закрытия</li>
 * <li>openPricesCache - для цен открытия</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class CacheInstrumentsService {

    /** Логгер для записи операций с кэшированными инструментами */
    private static final Logger logger = LoggerFactory.getLogger(CacheInstrumentsService.class);

    /** Репозиторий для работы с акциями */
    private final ShareRepository shareRepository;

    /** Репозиторий для работы с фьючерсами */
    private final FutureRepository futureRepository;

    /** Репозиторий для работы с индикативами */
    private final Indicativerepository indicativeRepository;

    /** Репозиторий для работы с ценами закрытия */
    private final ClosePriceRepository closePriceRepository;

    /** Репозиторий для работы с ценами открытия */
    private final OpenPriceRepositrory openPriceRepository;

    /** Репозиторий для работы с ценами закрытия вечерней сессии */
    private final ClosePriceEveningSessionRepository closePriceEveningSessionRepository;

    /** Менеджер кэша для управления кэшированием */
    private final CacheManager cacheManager;

    /** Маппер для преобразования Entity в DTO и обратно */
    @Autowired
    private Mapper mapper;

    /**
     * Конструктор сервиса кэшированных инструментов
     *
     * @param shareRepository      репозиторий для работы с акциями
     * @param futureRepository     репозиторий для работы с фьючерсами
     * @param indicativeRepository репозиторий для работы с индикативами
     * @param closePriceRepository репозиторий для работы с ценами закрытия
     * @param openPriceRepository  репозиторий для работы с ценами открытия
     * @param cacheManager         менеджер кэша
     */
    public CacheInstrumentsService(ShareRepository shareRepository, FutureRepository futureRepository,
            Indicativerepository indicativeRepository, ClosePriceRepository closePriceRepository,
            OpenPriceRepositrory openPriceRepository,
            ClosePriceEveningSessionRepository closePriceEveningSessionRepository, CacheManager cacheManager) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.closePriceRepository = closePriceRepository;
        this.openPriceRepository = openPriceRepository;
        this.closePriceEveningSessionRepository = closePriceEveningSessionRepository;
        this.cacheManager = cacheManager;

        // Инициализация конфигурации инструментов
        initializeInstrumentConfig();
    }

    /**
     * Получает список всех акций из кэша с fallback на базу данных
     *
     * <p>
     * Сначала пытается получить акции из кэша sharesCache. Если кэш пуст или
     * произошла ошибка, загружает данные из базы данных через ShareRepository.
     * </p>
     *
     * <p>
     * Процесс выполнения:
     * </p>
     * <ul>
     * <li>Попытка получения данных из кэша</li>
     * <li>При отсутствии данных в кэше - загрузка из БД</li>
     * <li>Преобразование Entity в DTO через Mapper</li>
     * <li>Логирование результатов операции</li>
     * </ul>
     *
     * @return список акций в формате ShareDTO
     */
    @Transactional
    public List<ShareDTO> getShares() {
        return getInstrumentData(InstrumentType.SHARES, false);
    }

    /**
     * Получает список всех фьючерсов из кэша с fallback на базу данных
     *
     * <p>
     * Сначала пытается получить фьючерсы из кэша futuresCache. Если кэш пуст или
     * произошла ошибка, загружает данные из базы данных через FutureRepository.
     * </p>
     *
     * <p>
     * Процесс выполнения:
     * </p>
     * <ul>
     * <li>Попытка получения данных из кэша</li>
     * <li>При отсутствии данных в кэше - загрузка из БД</li>
     * <li>Преобразование Entity в DTO через Mapper</li>
     * <li>Логирование результатов операции</li>
     * </ul>
     *
     * @return список фьючерсов в формате FutureDTO
     */
    public List<FutureDTO> getFutures() {
        return getInstrumentData(InstrumentType.FUTURES, false);
    }

    /**
     * Получает список всех индикативов из кэша с fallback на базу данных
     *
     * <p>
     * Сначала пытается получить индикативы из кэша indicativesCache. Если кэш пуст
     * или
     * произошла ошибка, загружает данные из базы данных через Indicativerepository.
     * </p>
     *
     * <p>
     * Процесс выполнения:
     * </p>
     * <ul>
     * <li>Попытка получения данных из кэша</li>
     * <li>При отсутствии данных в кэше - загрузка из БД</li>
     * <li>Преобразование Entity в DTO через Mapper</li>
     * <li>Логирование результатов операции</li>
     * </ul>
     *
     * @return список индикативов в формате IndicativeDTO
     */
    @Transactional
    public List<IndicativeDTO> getIndicatives() {
        return getInstrumentData(InstrumentType.INDICATIVES, false);
    }

    /**
     * Получает список всех акций ТОЛЬКО из кэша (без fallback на БД)
     *
     * <p>
     * Этот метод предназначен для случаев, когда нужно получить данные
     * исключительно из кэша без обращения к базе данных. Если кэш пуст,
     * возвращается пустой список.
     * </p>
     *
     * <p>
     * Использование:
     * </p>
     * <ul>
     * <li>Для API endpoints, которые должны работать только с кэшированными
     * данными</li>
     * <li>Для проверки состояния кэша</li>
     * <li>Для быстрого получения данных без нагрузки на БД</li>
     * </ul>
     *
     * @return список акций в формате ShareDTO (может быть пустым)
     */
    public List<ShareDTO> getSharesFromCacheOnly() {
        return getInstrumentData(InstrumentType.SHARES, true);
    }

    /**
     * Получает список всех фьючерсов ТОЛЬКО из кэша (без fallback на БД)
     *
     * <p>
     * Этот метод предназначен для случаев, когда нужно получить данные
     * исключительно из кэша без обращения к базе данных. Если кэш пуст,
     * возвращается пустой список.
     * </p>
     *
     * <p>
     * Использование:
     * </p>
     * <ul>
     * <li>Для API endpoints, которые должны работать только с кэшированными
     * данными</li>
     * <li>Для проверки состояния кэша</li>
     * <li>Для быстрого получения данных без нагрузки на БД</li>
     * </ul>
     *
     * @return список фьючерсов в формате FutureDTO (может быть пустым)
     */
    public List<FutureDTO> getFuturesFromCacheOnly() {
        return getInstrumentData(InstrumentType.FUTURES, true);
    }

    /**
     * Получает список всех индикативов ТОЛЬКО из кэша (без fallback на БД)
     *
     * <p>
     * Этот метод предназначен для случаев, когда нужно получить данные
     * исключительно из кэша без обращения к базе данных. Если кэш пуст,
     * возвращается пустой список.
     * </p>
     *
     * <p>
     * Использование:
     * </p>
     * <ul>
     * <li>Для API endpoints, которые должны работать только с кэшированными
     * данными</li>
     * <li>Для проверки состояния кэша</li>
     * <li>Для быстрого получения данных без нагрузки на БД</li>
     * </ul>
     *
     * @return список индикативов в формате IndicativeDTO (может быть пустым)
     */
    public List<IndicativeDTO> getIndicativesFromCacheOnly() {
        return getInstrumentData(InstrumentType.INDICATIVES, true);
    }

    /**
     * Получает все инструменты ТОЛЬКО из кэша (без fallback на БД)
     *
     * <p>
     * Этот метод объединяет получение всех типов инструментов только из кэша.
     * Если какой-либо кэш пуст, соответствующий список будет пустым.
     * </p>
     *
     * <p>
     * Использование:
     * </p>
     * <ul>
     * <li>Для API endpoints, которые должны работать только с кэшированными
     * данными</li>
     * <li>Для быстрого получения всех инструментов без нагрузки на БД</li>
     * <li>Для проверки состояния всех кэшей одновременно</li>
     * </ul>
     *
     * @return Map с инструментами и их размерами
     */
    public Map<String, Object> getAllInstrumentsFromCacheOnly() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Получаем данные для всех типов инструментов
            List<ShareDTO> shares = getInstrumentData(InstrumentType.SHARES, true);
            List<FutureDTO> futures = getInstrumentData(InstrumentType.FUTURES, true);
            List<IndicativeDTO> indicatives = getInstrumentData(InstrumentType.INDICATIVES, true);
            List<ClosePriceDTO> closePrices = getInstrumentData(InstrumentType.CLOSE_PRICES, true);
            List<OpenPriceDTO> openPrices = getInstrumentData(InstrumentType.OPEN_PRICES, true);
            List<ClosePriceEveningSessionDTO> closePriceEveningSessions = getInstrumentData(
                    InstrumentType.CLOSE_PRICES_EVENING_SESSION, true);

            // Формируем результат
            result.put("shares", shares);
            result.put("futures", futures);
            result.put("indicatives", indicatives);
            result.put("closePrices", closePrices);
            result.put("openPrices", openPrices);
            result.put("closePriceEveningSessions", closePriceEveningSessions);
            result.put("shares_size", shares.size());
            result.put("futures_size", futures.size());
            result.put("indicatives_size", indicatives.size());
            result.put("closePrices_size", closePrices.size());
            result.put("openPrices_size", openPrices.size());
            result.put("closePriceEveningSessions_size", closePriceEveningSessions.size());
            result.put("total_instruments",
                    shares.size() + futures.size() + indicatives.size() + closePrices.size() + openPrices.size()
                            + closePriceEveningSessions.size());

            logger.info(
                    "Получены инструменты только из кэша: {} акций, {} фьючерсов, {} индикативов, {} цен закрытия, {} цен открытия",
                    shares.size(), futures.size(), indicatives.size(), closePrices.size(), openPrices.size());

        } catch (Exception e) {
            logger.error("Ошибка при получении всех инструментов только из кэша: {}", e.getMessage(), e);
            // Возвращаем пустые результаты при ошибке
            Arrays.stream(InstrumentType.values()).forEach(type -> {
                String key = type.getCacheName().replace("Cache", "");
                result.put(key, new ArrayList<>());
                result.put(key + "_size", 0);
            });
            result.put("total_instruments", 0);
        }

        return result;
    }

    /**
     * Получает список всех цен закрытия из кэша с fallback на базу данных
     *
     * <p>
     * Сначала пытается получить цены закрытия из кэша closePricesCache. Если кэш
     * пуст или
     * произошла ошибка, загружает данные из базы данных через ClosePriceRepository.
     * </p>
     *
     * <p>
     * Процесс выполнения:
     * </p>
     * <ul>
     * <li>Попытка получения данных из кэша</li>
     * <li>При отсутствии данных в кэше - загрузка из БД</li>
     * <li>Преобразование Entity в DTO через Mapper</li>
     * <li>Логирование результатов операции</li>
     * </ul>
     *
     * @return список цен закрытия в формате ClosePriceDTO
     */
    @Transactional
    public List<ClosePriceDTO> getClosePrices() {
        return getInstrumentData(InstrumentType.CLOSE_PRICES, false);
    }

    /**
     * Получает список всех цен закрытия ТОЛЬКО из кэша (без fallback на БД)
     *
     * <p>
     * Этот метод предназначен для случаев, когда нужно получить данные
     * исключительно из кэша без обращения к базе данных. Если кэш пуст,
     * возвращается пустой список.
     * </p>
     *
     * <p>
     * Использование:
     * </p>
     * <ul>
     * <li>Для API endpoints, которые должны работать только с кэшированными
     * данными</li>
     * <li>Для проверки состояния кэша</li>
     * <li>Для быстрого получения данных без нагрузки на БД</li>
     * </ul>
     *
     * @return список цен закрытия в формате ClosePriceDTO (может быть пустым)
     */
    public List<ClosePriceDTO> getClosePricesFromCacheOnly() {
        return getInstrumentData(InstrumentType.CLOSE_PRICES, true);
    }

    /**
     * Получает список всех цен открытия из кэша с fallback на базу данных
     *
     * <p>
     * Сначала пытается получить цены открытия из кэша openPricesCache. Если кэш
     * пуст или произошла ошибка, загружает данные из базы данных через
     * OpenPriceRepository.
     * </p>
     *
     * <p>
     * Процесс выполнения:
     * </p>
     * <ul>
     * <li>Попытка получения данных из кэша</li>
     * <li>При отсутствии данных в кэше - загрузка из БД</li>
     * <li>Преобразование Entity в DTO через Mapper</li>
     * <li>Логирование результатов операции</li>
     * </ul>
     *
     * @return список цен открытия в формате OpenPriceDTO
     */
    @Transactional
    public List<OpenPriceDTO> getOpenPrices() {
        return getInstrumentData(InstrumentType.OPEN_PRICES, false);
    }

    /**
     * Получает список всех цен открытия ТОЛЬКО из кэша (без fallback на БД)
     *
     * <p>
     * Этот метод предназначен для случаев, когда нужно получить данные
     * исключительно из кэша без обращения к базе данных. Если кэш пуст,
     * возвращается пустой список.
     * </p>
     *
     * <p>
     * Использование:
     * </p>
     * <ul>
     * <li>Для API endpoints, которые должны работать только с кэшированными
     * данными</li>
     * <li>Для проверки состояния кэша</li>
     * <li>Для быстрого получения данных без нагрузки на БД</li>
     * </ul>
     *
     * @return список цен открытия в формате OpenPriceDTO (может быть пустым)
     */
    public List<OpenPriceDTO> getOpenPricesFromCacheOnly() {
        return getInstrumentData(InstrumentType.OPEN_PRICES, true);
    }

    /**
     * Получает список всех цен закрытия вечерней сессии из кэша с fallback на базу
     * данных
     *
     * @return список цен закрытия вечерней сессии в формате
     *         ClosePriceEveningSessionDTO
     */
    @Transactional
    public List<ClosePriceEveningSessionDTO> getClosePriceEveningSessions() {
        return getInstrumentData(InstrumentType.CLOSE_PRICES_EVENING_SESSION, false);
    }

    /**
     * Получает список всех цен закрытия вечерней сессии ТОЛЬКО из кэша (без
     * fallback на БД)
     *
     * @return список цен закрытия вечерней сессии в формате
     *         ClosePriceEveningSessionDTO (может быть пустым)
     */
    public List<ClosePriceEveningSessionDTO> getClosePriceEveningSessionsFromCacheOnly() {
        return getInstrumentData(InstrumentType.CLOSE_PRICES_EVENING_SESSION, true);
    }

    // ===========================================
    // Универсальные методы для работы с кэшем
    // ===========================================

    /**
     * Универсальный метод для поиска данных в кэше
     *
     * <p>
     * Выполняет поиск данных в указанном кэше по различным возможным ключам.
     * Если стандартные ключи не найдены, пытается получить все записи
     * из кэша через нативный интерфейс Caffeine.
     * </p>
     *
     * @param <T>            тип DTO данных
     * @param instrumentType тип инструмента для поиска
     * @return список найденных DTO или пустой список
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> getDataFromCache(InstrumentType instrumentType) {
        Cache cache = cacheManager.getCache(instrumentType.getCacheName());
        if (cache == null) {
            logger.warn("Кэш {} не найден", instrumentType.getCacheName());
            return Collections.emptyList();
        }

        List<T> allData = new ArrayList<>();

        try {
            String[] possibleKeys = instrumentType.getPossibleKeys();
            logger.debug("Поиск данных в кэше {} по ключам: {}",
                    instrumentType.getCacheName(), String.join(", ", possibleKeys));

            // Поиск по стандартным ключам
            for (String cacheKey : possibleKeys) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null && wrapper.get() instanceof List) {
                    List<T> data = (List<T>) wrapper.get();
                    if (data != null && !data.isEmpty()) {
                        allData.addAll(data);
                        logger.info("Найдено {} записей в кэше {} с ключом: {}",
                                data.size(), instrumentType.getCacheName(), cacheKey);
                        break; // Используем первый найденный ключ
                    }
                } else {
                    logger.debug("Ключ '{}' не найден в кэше {} или содержит неверный тип данных",
                            cacheKey, instrumentType.getCacheName());
                }
            }

            // Поиск через нативный интерфейс Caffeine
            if (allData.isEmpty() && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                logger.debug("Поиск данных через нативный интерфейс Caffeine для кэша {}",
                        instrumentType.getCacheName());
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache
                        .getNativeCache();

                for (Map.Entry<?, ?> entry : caffeineCache.asMap().entrySet()) {
                    logger.debug("Проверка ключа в нативном кэше {}: {}",
                            instrumentType.getCacheName(), entry.getKey());
                    if (entry.getValue() instanceof List) {
                        List<T> data = (List<T>) entry.getValue();
                        if (data != null && !data.isEmpty()) {
                            allData.addAll(data);
                            logger.info("Найдено {} записей в нативном кэше {} с ключом: {}",
                                    data.size(), instrumentType.getCacheName(), entry.getKey());
                            break; // Используем первый найденный список
                        }
                    }
                }
            }

            if (allData.isEmpty()) {
                logger.warn("Данные не найдены в кэше {} ни по одному из ключей",
                        instrumentType.getCacheName());
            }

        } catch (Exception e) {
            logger.error("Ошибка получения данных из кэша {}: {}",
                    instrumentType.getCacheName(), e.getMessage(), e);
        }

        return allData;
    }

    /**
     * Инициализирует конфигурацию инструментов
     */
    private void initializeInstrumentConfig() {
        InstrumentConfig.SHARES.setDbSupplier(() -> shareRepository.findAll());
        InstrumentConfig.SHARES.setMapperFunction((List<ShareEntity> entities) -> mapper.toShareDTOList(entities));

        InstrumentConfig.FUTURES.setDbSupplier(() -> futureRepository.findAll());
        InstrumentConfig.FUTURES.setMapperFunction((List<FutureEntity> entities) -> mapper.toFutureDTOList(entities));

        InstrumentConfig.INDICATIVES.setDbSupplier(() -> indicativeRepository.findAll());
        InstrumentConfig.INDICATIVES
                .setMapperFunction((List<IndicativeEntity> entities) -> mapper.toIndicativeDTOList(entities));

        InstrumentConfig.CLOSE_PRICES.setDbSupplier(
                () -> closePriceRepository.findById_PriceDate(WorkingDaysUtils.getPreviousWorkingDay(LocalDate.now())));
        InstrumentConfig.CLOSE_PRICES
                .setMapperFunction((List<ClosePriceEntity> entities) -> mapper.toClosePriceDTOList(entities));

        InstrumentConfig.OPEN_PRICES.setDbSupplier(
                () -> openPriceRepository.findById_PriceDate(WorkingDaysUtils.getPreviousWorkingDay(LocalDate.now())));
        InstrumentConfig.OPEN_PRICES
                .setMapperFunction((List<OpenPriceEntity> entities) -> mapper.toOpenPriceDTOList(entities));

        InstrumentConfig.CLOSE_PRICES_EVENING_SESSION.setDbSupplier(
                () -> closePriceEveningSessionRepository
                        .findByPriceDate(WorkingDaysUtils.getPreviousWorkingDay(LocalDate.now())));
        InstrumentConfig.CLOSE_PRICES_EVENING_SESSION
                .setMapperFunction((List<ClosePriceEveningSessionEntity> entities) -> mapper
                        .toClosePriceEveningSessionDTOList(entities));
    }

    /**
     * Универсальный метод для получения данных инструментов
     *
     * @param <T>            тип DTO данных
     * @param instrumentType тип инструмента
     * @param cacheOnly      если true, не использует fallback на БД
     * @return список DTO данных
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> getInstrumentData(InstrumentType instrumentType, boolean cacheOnly) {
        List<T> cachedData = getDataFromCache(instrumentType);

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
