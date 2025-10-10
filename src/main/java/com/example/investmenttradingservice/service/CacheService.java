package com.example.investmenttradingservice.service;

import com.example.investmenttradingservice.entity.ClosePriceEntity;
import com.example.investmenttradingservice.entity.ClosePriceEveningSessionEntity;
import com.example.investmenttradingservice.entity.DividendEntity;
import com.example.investmenttradingservice.entity.FutureEntity;
import com.example.investmenttradingservice.entity.IndicativeEntity;
import com.example.investmenttradingservice.entity.LastPriceEntity;
import com.example.investmenttradingservice.entity.OpenPriceEntity;
import com.example.investmenttradingservice.entity.ShareEntity;
import com.example.investmenttradingservice.mapper.Mapper;
import com.example.investmenttradingservice.repository.ClosePriceEveningSessionRepository;
import com.example.investmenttradingservice.repository.ClosePriceRepository;
import com.example.investmenttradingservice.repository.DivedendsRepository;
import com.example.investmenttradingservice.repository.FutureRepository;
import com.example.investmenttradingservice.repository.OpenPriceRepositrory;
import com.example.investmenttradingservice.repository.ShareRepository;
import com.example.investmenttradingservice.repository.Indicativerepository;
import com.example.investmenttradingservice.repository.LastPriceRepository;
import com.example.investmenttradingservice.util.TimeZoneUtils;
import com.example.investmenttradingservice.util.WorkingDaysUtils;
import com.example.investmenttradingservice.enums.CacheConfig;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для прогрева кэша из базы данных при запуске приложения
 *
 * <p>
 * Этот сервис выполняет загрузку основных инструментов из базы данных в кэш
 * после
 * полной инициализации Spring контекста. Это обеспечивает быстрый отклик при
 * первом обращении к API без необходимости обращения к внешним сервисам.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Автоматический прогрев кэша при запуске приложения</li>
 * <li>Ручной прогрев кэша по требованию</li>
 * <li>Загрузка акций из базы данных в кэш</li>
 * <li>Загрузка фьючерсов из базы данных в кэш</li>
 * <li>Логирование процесса прогрева для мониторинга</li>
 * </ul>
 *
 * <p>
 * Сервис использует следующие кэши:
 * </p>
 * <ul>
 * <li>sharesCache - для акций</li>
 * <li>futuresCache - для фьючерсов</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class CacheService {

    /** Логгер для записи операций с кэшем */
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

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

    /** Репозиторий для работы с дивидентами */
    private final DivedendsRepository divedendsRepository;

    private final LastPriceRepository lastPriceRepository;

    /** Менеджер кэша для управления кэшированием */
    private final CacheManager cacheManager;

    /** Маппер для преобразования Entity в DTO и обратно */
    @Autowired
    private Mapper mapper;

    /**
     * Конструктор сервиса прогрева кэша
     *
     * @param shareRepository      репозиторий для работы с акциями
     * @param futureRepository     репозиторий для работы с фьючерсами
     * @param indicativeRepository репозиторий для работы с индикативами
     * @param closePriceRepository репозиторий для работы с ценами закрытия
     * @param openPriceRepository  репозиторий для работы с ценами открытия
     * @param cacheManager         менеджер кэша
     */
    public CacheService(ShareRepository shareRepository,
            FutureRepository futureRepository,
            Indicativerepository indicativeRepository,
            ClosePriceRepository closePriceRepository,
            OpenPriceRepositrory openPriceRepository,
            ClosePriceEveningSessionRepository closePriceEveningSessionRepository,
            DivedendsRepository divedendsRepository,
            LastPriceRepository lastPriceRepository,
            CacheManager cacheManager) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.closePriceRepository = closePriceRepository;
        this.openPriceRepository = openPriceRepository;
        this.closePriceEveningSessionRepository = closePriceEveningSessionRepository;
        this.divedendsRepository = divedendsRepository;
        this.lastPriceRepository = lastPriceRepository;
        this.cacheManager = cacheManager;

        // Инициализация конфигурации кэшей
        initializeCacheConfig();
    }

    /**
     * Автоматический прогрев кэша при запуске приложения
     *
     * <p>
     * Выполняется после полной инициализации Spring контекста.
     * Загружает основные инструменты из базы данных в кэш для обеспечения
     * быстрого отклика API.
     * </p>
     *
     * <p>
     * Процесс прогрева включает:
     * </p>
     * <ul>
     * <li>Загрузку всех акций из базы данных в кэш sharesCache</li>
     * <li>Загрузку всех фьючерсов из базы данных в кэш futuresCache</li>
     * <li>Логирование результатов для мониторинга</li>
     * </ul>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCacheOnStartup() {
        String taskId = "STARTUP_WARMUP_" + LocalDateTime.now(TimeZoneUtils.getMoscowZone());
        logger.info("[{}] Начало прогрева кэша при запуске приложения", taskId);

        try {
            warmupCacheUniversal(taskId, false);
        } catch (Exception e) {
            logger.error("[{}] Ошибка при прогреве кэша при запуске: {}", taskId, e.getMessage(), e);
        }
    }

    /**
     * Ручной прогрев кэша (для тестирования или принудительного обновления)
     *
     * <p>
     * Может быть вызван через REST API или другие сервисы для принудительного
     * прогрева кэша без перезапуска приложения.
     * </p>
     *
     * <p>
     * Процесс включает:
     * </p>
     * <ul>
     * <li>Очистку существующих кэшей</li>
     * <li>Загрузку свежих данных из базы данных</li>
     * <li>Логирование результатов</li>
     * </ul>
     *
     * @throws Exception если произошла ошибка при прогреве кэша
     */
    public void manualWarmupCache() throws Exception {
        String taskId = "MANUAL_WARMUP_" + LocalDateTime.now(TimeZoneUtils.getMoscowZone());
        logger.info("[{}] Начало ручного прогрева кэша", taskId);

        try {
            warmupCacheUniversal(taskId, true);
        } catch (Exception e) {
            logger.error("[{}] Ошибка при ручном прогреве кэша: {}", taskId, e.getMessage(), e);
            throw new Exception("Ошибка при ручном прогреве кэша", e);
        }
    }

    /**
     * Очищает все кэши
     *
     * <p>
     * Удаляет все данные из кэшей sharesCache и futuresCache.
     * Используется при ручном прогреве для обеспечения свежести данных.
     * </p>
     * 
     * @throws Exception
     */
    public void clearAllCaches() throws Exception {
        try {
            clearAllCachesUniversal();
        } catch (Exception e) {
            logger.error("Ошибка при очистке кэшей: {}", e.getMessage(), e);
            throw new Exception("Ошибка при очистке кэшей", e);
        }
    }

    /**
     * Универсальный метод для получения размера данных в кэше
     *
     * @param cacheName имя кэша
     * @param key       ключ для получения данных
     * @return количество элементов в кэше
     */
    private int getCacheSize(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null && wrapper.get() instanceof List) {
                    List<?> data = (List<?>) wrapper.get();
                    return data != null ? data.size() : 0;
                }
            }
        } catch (Exception e) {
            logger.debug("Ошибка при получении размера кэша {}: {}", cacheName, e.getMessage());
        }
        return 0;
    }

    public Map<String, Object> getCacheStats() {

        Map<String, Object> cacheStats = new HashMap<>();
        for (CacheConfig config : CacheConfig.values()) {
            Cache cache = cacheManager.getCache(config.getCacheName());
            if (cache != null) {
                cacheStats.put(config.getCacheName(), getCacheSize(config.getCacheName(), config.getCacheKey()));
            }
        }
        return cacheStats;
    }

    /**
     * Инициализирует конфигурацию кэшей
     */
    private void initializeCacheConfig() {
        CacheConfig.SHARES.setMapperFunction((List<ShareEntity> entities) -> mapper.toShareDTOList(entities));
        CacheConfig.FUTURES.setMapperFunction((List<FutureEntity> entities) -> mapper.toFutureDTOList(entities));
        CacheConfig.INDICATIVES
                .setMapperFunction((List<IndicativeEntity> entities) -> mapper.toIndicativeDTOList(entities));
        CacheConfig.CLOSE_PRICES
                .setMapperFunction((List<ClosePriceEntity> entities) -> mapper.toClosePriceDTOList(entities));
        CacheConfig.OPEN_PRICES
                .setMapperFunction((List<OpenPriceEntity> entities) -> mapper.toOpenPriceDTOList(entities));
        CacheConfig.CLOSE_PRICES_EVENING_SESSION
                .setMapperFunction((List<ClosePriceEveningSessionEntity> entities) -> mapper
                        .toClosePriceEveningSessionDTOList(entities));

        CacheConfig.LAST_PRICES
                .setMapperFunction((List<LastPriceEntity> entities) -> mapper.toLastPriceDTOList(entities));

        CacheConfig.DIVIDEND
                .setMapperFunction((List<DividendEntity> entities) -> mapper.toDividendDtoList(entities));
    }

    /**
     * Универсальный метод для загрузки данных в кэш
     *
     * @param <T>         тип Entity данных
     * @param <R>         тип DTO данных
     * @param cacheConfig конфигурация кэша
     * @param entities    список Entity для загрузки
     */
    @SuppressWarnings("unchecked")
    private <T, R> void loadDataToCache(CacheConfig cacheConfig, List<T> entities) {
        try {
            Cache cache = cacheManager.getCache(cacheConfig.getCacheName());
            if (cache != null) {
                Function<List<T>, List<R>> mapper = (Function<List<T>, List<R>>) cacheConfig.getMapperFunction();
                List<R> dtos = mapper.apply(entities);
                cache.put(cacheConfig.getCacheKey(), dtos);
                logger.info("{} успешно загружены в кэш {} (количество: {})",
                        cacheConfig.getDescription(), cacheConfig.getCacheName(), entities.size());
            } else {
                logger.error("Кэш {} не найден", cacheConfig.getCacheName());
            }
        } catch (Exception e) {
            logger.error("Ошибка при загрузке {} в кэш: {}", cacheConfig.getDescription(), e.getMessage(), e);
        }
    }

    /**
     * Универсальный метод для очистки всех кэшей
     */
    private void clearAllCachesUniversal() {
        for (CacheConfig config : CacheConfig.values()) {
            try {
                Cache cache = cacheManager.getCache(config.getCacheName());
                if (cache != null) {
                    cache.clear();
                    logger.info("Кэш {} очищен", config.getCacheName());
                }
            } catch (Exception e) {
                logger.error("Ошибка при очистке кэша {}: {}", config.getCacheName(), e.getMessage(), e);
            }
        }
        logger.info("Все кэши успешно очищены");
    }

    /**
     * Универсальный метод для прогрева кэша
     *
     * @param taskId          идентификатор задачи для логирования
     * @param clearCacheFirst если true, очищает кэши перед прогревом
     */
    private void warmupCacheUniversal(String taskId, boolean clearCacheFirst) {
        try {
            if (clearCacheFirst) {
                clearAllCachesUniversal();
            }

            // Прогрев кэша акций
            logger.info("[{}] Прогрев кэша акций из базы данных", taskId);
            List<ShareEntity> shares = shareRepository.findAll();
            if (shares != null && !shares.isEmpty()) {
                loadDataToCache(CacheConfig.SHARES, shares);
                logger.info("[{}] В кэш загружено акций: {}", taskId, shares.size());
            } else {
                logger.warn("[{}] В базе данных не найдено акций", taskId);
            }

            // Прогрев кэша фьючерсов
            logger.info("[{}] Прогрев кэша фьючерсов из базы данных", taskId);
            List<FutureEntity> futures = futureRepository.findAll();
            if (futures != null && !futures.isEmpty()) {
                loadDataToCache(CacheConfig.FUTURES, futures);
                logger.info("[{}] В кэш загружено фьючерсов: {}", taskId, futures.size());
            } else {
                logger.warn("[{}] В базе данных не найдено фьючерсов", taskId);
            }

            // Прогрев кэша индикативов
            logger.info("[{}] Прогрев кэша индикативов из базы данных", taskId);
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            if (indicatives != null && !indicatives.isEmpty()) {
                loadDataToCache(CacheConfig.INDICATIVES, indicatives);
                logger.info("[{}] В кэш загружено индикативов: {}", taskId, indicatives.size());
            } else {
                logger.warn("[{}] В базе данных не найдено индикативов", taskId);
            }

            // Прогрев кэша цен закрытия
            logger.info("[{}] Прогрев кэша цен закрытия основной сессии из базы данных", taskId);
            List<ClosePriceEntity> closePrices = closePriceRepository
                    .findById_PriceDate(WorkingDaysUtils.getPreviousWorkingDay(LocalDate.now()));
            if (closePrices != null && !closePrices.isEmpty()) {
                loadDataToCache(CacheConfig.CLOSE_PRICES, closePrices);
                logger.info("[{}] В кэш загружено цен закрытия: {}", taskId, closePrices.size());
            } else {
                logger.warn("[{}] В базе данных не найдено цен закрытия", taskId);
            }

            // Прогрев кэша цен открытия
            logger.info("[{}] Прогрев кэша цен открытия основной сессии из базы данных", taskId);
            List<OpenPriceEntity> openPrices = openPriceRepository
                    .findById_PriceDate(WorkingDaysUtils.getPreviousWorkingDay(LocalDate.now()));
            if (openPrices != null && !openPrices.isEmpty()) {
                loadDataToCache(CacheConfig.OPEN_PRICES, openPrices);
                logger.info("[{}] В кэш загружено цен открытия: {}", taskId, openPrices.size());
            } else {
                logger.warn("[{}] В базе данных не найдено цен открытия", taskId);
            }

            // Прогрев кэша цен закрытия вечерней сессии
            logger.info("[{}] Прогрев кэша цен закрытия вечерней сессии из базы данных", taskId);
            List<ClosePriceEveningSessionEntity> closePriceEveningSessions = closePriceEveningSessionRepository
                    .findByPriceDate(WorkingDaysUtils.getPreviousWorkingDay(LocalDate.now()));
            if (closePriceEveningSessions != null && !closePriceEveningSessions.isEmpty()) {
                loadDataToCache(CacheConfig.CLOSE_PRICES_EVENING_SESSION, closePriceEveningSessions);
                logger.info("[{}] В кэш загружено цен закрытия вечерней сессии: {}", taskId,
                        closePriceEveningSessions.size());
            } else {
                logger.warn("[{}] В базе данных не найдено цен закрытия вечерней сессии", taskId);
            }

            // Прогрев кэша для цен последних сделок
            logger.info("[{}] Прогрев кэша цен последних сделок из базы данных", taskId);
            List<LastPriceEntity> lastPrices = lastPriceRepository
                    .findAllLatestPrices();
            if (lastPrices != null && !lastPrices.isEmpty()) {
                loadDataToCache(CacheConfig.LAST_PRICES, lastPrices);
                logger.info("[{}] В кэш загружено цен последних сделок по инструментам: {}", taskId,
                        lastPrices.size());
            } else {
                logger.warn("[{}] В базе данных не найдено цен последних сделок", taskId);
            }

            // Прогрев кэша для дивидендов
            logger.info("[{}] Прогрев кэша дивидендов из базы данных", taskId);
            List<DividendEntity> dividends = divedendsRepository
                    .findDividendsForTodayAndTomorrow();
            if (dividends != null && !dividends.isEmpty()) {
                loadDataToCache(CacheConfig.DIVIDEND, dividends);
                logger.info("[{}] В кэш загружено дивидендов на сегодня/завтра: {}", taskId,
                        dividends.size());
            } else {
                logger.warn("[{}] В базе данных не найдено дивидендов на сегодня/завтра", taskId);
            }

            logger.info("[{}] Прогрев кэша завершен успешно", taskId);

        } catch (Exception e) {
            logger.error("[{}] Ошибка при прогреве кэша: {}", taskId, e.getMessage(), e);
            throw new RuntimeException("Ошибка при прогреве кэша", e);
        }
    }
}
