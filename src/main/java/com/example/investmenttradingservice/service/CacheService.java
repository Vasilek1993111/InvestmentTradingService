package com.example.investmenttradingservice.service;

import com.example.investmenttradingservice.DTO.FutureDTO;
import com.example.investmenttradingservice.DTO.ShareDTO;
import com.example.investmenttradingservice.DTO.IndicativeDTO;
import com.example.investmenttradingservice.Entity.FutureEntity;
import com.example.investmenttradingservice.Entity.ShareEntity;
import com.example.investmenttradingservice.Entity.IndicativeEntity;
import com.example.investmenttradingservice.mapper.Mapper;
import com.example.investmenttradingservice.repository.FutureRepository;
import com.example.investmenttradingservice.repository.ShareRepository;
import com.example.investmenttradingservice.repository.Indicativerepository;
import com.example.investmenttradingservice.util.TimeZoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

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
     * @param cacheManager         менеджер кэша
     */
    public CacheService(ShareRepository shareRepository, FutureRepository futureRepository,
            Indicativerepository indicativeRepository, CacheManager cacheManager) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.cacheManager = cacheManager;
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
            // Прогрев кэша акций
            logger.info("[{}] Прогрев кэша акций из базы данных", taskId);
            List<ShareEntity> shares = shareRepository.findAll();
            if (shares != null && !shares.isEmpty()) {
                loadSharesToCache(shares);
                logger.info("[{}] В кэш загружено акций: {}", taskId, shares.size());
            } else {
                logger.warn("[{}] В базе данных не найдено акций", taskId);
            }

            // Прогрев кэша фьючерсов
            logger.info("[{}] Прогрев кэша фьючерсов из базы данных", taskId);
            List<FutureEntity> futures = futureRepository.findAll();
            if (futures != null && !futures.isEmpty()) {
                loadFuturesToCache(futures);
                logger.info("[{}] В кэш загружено фьючерсов: {}", taskId, futures.size());
            } else {
                logger.warn("[{}] В базе данных не найдено фьючерсов", taskId);
            }

            // Прогрев кэша индикативов
            logger.info("[{}] Прогрев кэша индикативов из базы данных", taskId);
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            if (indicatives != null && !indicatives.isEmpty()) {
                loadIndicativesToCache(indicatives);
                logger.info("[{}] В кэш загружено индикативов: {}", taskId, indicatives.size());
            } else {
                logger.warn("[{}] В базе данных не найдено индикативов", taskId);
            }

            logger.info("[{}] Прогрев кэша при запуске завершен успешно", taskId);

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
            // Очищаем существующие кэши
            clearAllCaches();

            // Прогрев кэша акций
            logger.info("[{}] Прогрев кэша акций", taskId);
            List<ShareEntity> shares = shareRepository.findAll();
            if (shares != null && !shares.isEmpty()) {
                loadSharesToCache(shares);
                logger.info("[{}] В кэш загружено акций: {}", taskId, shares.size());
            } else {
                logger.warn("[{}] В базе данных не найдено акций", taskId);
            }

            // Прогрев кэша фьючерсов
            logger.info("[{}] Прогрев кэша фьючерсов", taskId);
            List<FutureEntity> futures = futureRepository.findAll();
            if (futures != null && !futures.isEmpty()) {
                loadFuturesToCache(futures);
                logger.info("[{}] В кэш загружено фьючерсов: {}", taskId, futures.size());
            } else {
                logger.warn("[{}] В базе данных не найдено фьючерсов", taskId);
            }

            // Прогрев кэша индикативов
            logger.info("[{}] Прогрев кэша индикативов", taskId);
            List<IndicativeEntity> indicatives = indicativeRepository.findAll();
            if (indicatives != null && !indicatives.isEmpty()) {
                loadIndicativesToCache(indicatives);
                logger.info("[{}] В кэш загружено индикативов: {}", taskId, indicatives.size());
            } else {
                logger.warn("[{}] В базе данных не найдено индикативов", taskId);
            }

            logger.info("[{}] Ручной прогрев кэша завершен успешно", taskId);

        } catch (Exception e) {
            logger.error("[{}] Ошибка при ручном прогреве кэша: {}", taskId, e.getMessage(), e);
            throw new Exception("Ошибка при ручном прогреве кэша", e);
        }
    }

    /**
     * Загружает акции в кэш
     *
     * <p>
     * Преобразует список ShareEntity в ShareDTO и загружает их в кэш sharesCache
     * с ключом "all_shares".
     * </p>
     *
     * @param shares список акций для загрузки в кэш
     */
    private void loadSharesToCache(List<ShareEntity> shares) {
        try {
            Cache sharesCache = cacheManager.getCache("sharesCache");
            if (sharesCache != null) {
                List<ShareDTO> shareDTOs = mapper.toShareDTOList(shares);
                sharesCache.put("all_shares", shareDTOs);
                logger.info("Акции успешно загружены в кэш sharesCache (количество: {})", shares.size());
            } else {
                logger.error("Кэш sharesCache не найден");
            }
        } catch (Exception e) {
            logger.error("Ошибка при загрузке акций в кэш: {}", e.getMessage(), e);
        }
    }

    /**
     * Загружает фьючерсы в кэш
     *
     * <p>
     * Преобразует список FutureEntity в FutureDTO и загружает их в кэш futuresCache
     * с ключом "all_futures".
     * </p>
     *
     * @param futures список фьючерсов для загрузки в кэш
     */
    private void loadFuturesToCache(List<FutureEntity> futures) {
        try {
            Cache futuresCache = cacheManager.getCache("futuresCache");
            if (futuresCache != null) {
                List<FutureDTO> futureDTOs = mapper.toFutureDTOList(futures);
                futuresCache.put("all_futures", futureDTOs);
                logger.info("Фьючерсы успешно загружены в кэш futuresCache (количество: {})", futures.size());
            } else {
                logger.error("Кэш futuresCache не найден");
            }
        } catch (Exception e) {
            logger.error("Ошибка при загрузке фьючерсов в кэш: {}", e.getMessage(), e);
        }
    }

    /**
     * Загружает индикативы в кэш indicativesCache
     *
     * <p>
     * Преобразует список IndicativeEntity в список IndicativeDTO и сохраняет
     * в кэш с ключом "all_indicatives".
     * </p>
     *
     * @param indicatives список индикативов для загрузки в кэш
     */
    private void loadIndicativesToCache(List<IndicativeEntity> indicatives) {
        try {
            Cache indicativesCache = cacheManager.getCache("indicativesCache");
            if (indicativesCache != null) {
                List<IndicativeDTO> indicativeDTOs = mapper.toIndicativeDTOList(indicatives);
                indicativesCache.put("all_indicatives", indicativeDTOs);
                logger.info("Индикативы успешно загружены в кэш indicativesCache (количество: {})", indicatives.size());
            } else {
                logger.error("Кэш indicativesCache не найден");
            }
        } catch (Exception e) {
            logger.error("Ошибка при загрузке индикативов в кэш: {}", e.getMessage(), e);
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
            Cache sharesCache = cacheManager.getCache("sharesCache");
            if (sharesCache != null) {
                sharesCache.clear();
                logger.info("Кэш sharesCache очищен");
            }

            Cache futuresCache = cacheManager.getCache("futuresCache");
            if (futuresCache != null) {
                futuresCache.clear();
                logger.info("Кэш futuresCache очищен");
            }

            Cache indicativesCache = cacheManager.getCache("indicativesCache");
            if (indicativesCache != null) {
                indicativesCache.clear();
                logger.info("Кэш indicativesCache очищен");
            }

            logger.info("Все кэши успешно очищены");
        } catch (Exception e) {

            logger.error("Ошибка при очистке кэшей: {}", e.getMessage(), e);
            throw new Exception("Ошибка при очистке кэшей", e);
        }
    }

    /**
     * Получает подробную статистику кэша
     *
     * <p>
     * Возвращает детальную информацию о текущем состоянии всех кэшей системы:
     * </p>
     * <ul>
     * <li>Количество записей в каждом кэше</li>
     * <li>Используемые ключи в кэшах</li>
     * <li>Статистика производительности Caffeine</li>
     * <li>Время последнего обновления</li>
     * <li>Общий размер данных в памяти</li>
     * </ul>
     *
     * @return строка с подробной информацией о состоянии кэшей
     */
    public String getCacheStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Подробная статистика кэша ===\n");
        stats.append("Время генерации: ").append(LocalDateTime.now(TimeZoneUtils.getMoscowZone())).append("\n\n");

        try {
            // Статистика кэша акций
            stats.append("📊 КЭШ АКЦИЙ (sharesCache):\n");
            Cache sharesCache = cacheManager.getCache("sharesCache");
            if (sharesCache != null) {
                Object nativeCache = sharesCache.getNativeCache();
                if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                    @SuppressWarnings("rawtypes")
                    com.github.benmanes.caffeine.cache.Cache caffeineCache = (com.github.benmanes.caffeine.cache.Cache) nativeCache;

                    stats.append("  • Количество записей: ").append(caffeineCache.estimatedSize()).append("\n");

                    // Получаем количество акций из кэша
                    int sharesCount = getSharesCountFromCache();
                    stats.append("  • Количество акций в кэше: ").append(sharesCount).append("\n");

                    stats.append("  • Статистика производительности:\n");
                    stats.append("    - Hit Rate: ")
                            .append(String.format("%.2f%%", caffeineCache.stats().hitRate() * 100)).append("\n");
                    stats.append("    - Miss Rate: ")
                            .append(String.format("%.2f%%", caffeineCache.stats().missRate() * 100)).append("\n");
                    stats.append("    - Eviction Count: ").append(caffeineCache.stats().evictionCount()).append("\n");

                    // Показываем ключи в кэше
                    stats.append("  • Ключи в кэше:\n");
                    for (Object key : caffeineCache.asMap().keySet()) {
                        stats.append("    - '").append(key).append("'\n");
                    }
                } else {
                    stats.append("  • Нативный кэш недоступен\n");
                }
            } else {
                stats.append("  • Кэш не найден\n");
            }

            stats.append("\n");

            // Статистика кэша фьючерсов
            stats.append("📈 КЭШ ФЬЮЧЕРСОВ (futuresCache):\n");
            Cache futuresCache = cacheManager.getCache("futuresCache");
            if (futuresCache != null) {
                Object nativeCache = futuresCache.getNativeCache();
                if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                    @SuppressWarnings("rawtypes")
                    com.github.benmanes.caffeine.cache.Cache caffeineCache = (com.github.benmanes.caffeine.cache.Cache) nativeCache;

                    stats.append("  • Количество записей: ").append(caffeineCache.estimatedSize()).append("\n");

                    // Получаем количество фьючерсов из кэша
                    int futuresCount = getFuturesCountFromCache();
                    stats.append("  • Количество фьючерсов в кэше: ").append(futuresCount).append("\n");

                    stats.append("  • Статистика производительности:\n");
                    stats.append("    - Hit Rate: ")
                            .append(String.format("%.2f%%", caffeineCache.stats().hitRate() * 100)).append("\n");
                    stats.append("    - Miss Rate: ")
                            .append(String.format("%.2f%%", caffeineCache.stats().missRate() * 100)).append("\n");
                    stats.append("    - Eviction Count: ").append(caffeineCache.stats().evictionCount()).append("\n");

                    // Показываем ключи в кэше
                    stats.append("  • Ключи в кэше:\n");
                    for (Object key : caffeineCache.asMap().keySet()) {
                        stats.append("    - '").append(key).append("'\n");
                    }
                } else {
                    stats.append("  • Нативный кэш недоступен\n");
                }
            } else {
                stats.append("  • Кэш не найден\n");
            }

            stats.append("\n");

            // Статистика кэша индикативов
            stats.append("📋 КЭШ ИНДИКАТИВОВ (indicativesCache):\n");
            Cache indicativesCache = cacheManager.getCache("indicativesCache");
            if (indicativesCache != null) {
                Object nativeCache = indicativesCache.getNativeCache();
                if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                    @SuppressWarnings("rawtypes")
                    com.github.benmanes.caffeine.cache.Cache caffeineCache = (com.github.benmanes.caffeine.cache.Cache) nativeCache;

                    stats.append("  • Количество записей: ").append(caffeineCache.estimatedSize()).append("\n");

                    // Получаем количество индикативов из кэша
                    int indicativesCount = getIndicativesCountFromCache();
                    stats.append("  • Количество индикативов в кэше: ").append(indicativesCount).append("\n");

                    stats.append("  • Статистика производительности:\n");
                    stats.append("    - Hit Rate: ")
                            .append(String.format("%.2f%%", caffeineCache.stats().hitRate() * 100)).append("\n");
                    stats.append("    - Miss Rate: ")
                            .append(String.format("%.2f%%", caffeineCache.stats().missRate() * 100)).append("\n");
                    stats.append("    - Eviction Count: ").append(caffeineCache.stats().evictionCount()).append("\n");

                    // Показываем ключи в кэше
                    stats.append("  • Ключи в кэше:\n");
                    for (Object key : caffeineCache.asMap().keySet()) {
                        stats.append("    - '").append(key).append("'\n");
                    }
                } else {
                    stats.append("  • Нативный кэш недоступен\n");
                }
            } else {
                stats.append("  • Кэш не найден\n");
            }

            stats.append("\n");

            // Общая статистика
            stats.append("🔍 ОБЩАЯ ИНФОРМАЦИЯ:\n");
            stats.append("  • Всего кэшей: 3 (sharesCache, futuresCache, indicativesCache)\n");
            stats.append("  • Тип кэша: Caffeine Cache\n");
            stats.append("  • Конфигурация: maximumSize=1000, expireAfterWrite=10m, expireAfterAccess=5m\n");

            // Сводка по количеству инструментов
            int totalShares = getSharesCountFromCache();
            int totalFutures = getFuturesCountFromCache();
            int totalIndicatives = getIndicativesCountFromCache();
            int totalInstruments = totalShares + totalFutures + totalIndicatives;

            stats.append("\n📈 СВОДКА ПО ИНСТРУМЕНТАМ:\n");
            stats.append("  • Акций в кэше: ").append(totalShares).append("\n");
            stats.append("  • Фьючерсов в кэше: ").append(totalFutures).append("\n");
            stats.append("  • Индикативов в кэше: ").append(totalIndicatives).append("\n");
            stats.append("  • Всего инструментов в кэше: ").append(totalInstruments).append("\n");

        } catch (Exception e) {
            stats.append("❌ Ошибка при получении статистики кэша: ").append(e.getMessage()).append("\n");
            logger.error("Ошибка при получении статистики кэша: {}", e.getMessage(), e);
        }

        return stats.toString();
    }

    /**
     * Получает количество акций из кэша
     *
     * @return количество акций в кэше
     */
    private int getSharesCountFromCache() {
        try {
            Cache sharesCache = cacheManager.getCache("sharesCache");
            if (sharesCache != null) {
                Cache.ValueWrapper wrapper = sharesCache.get("all_shares");
                if (wrapper != null && wrapper.get() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<ShareDTO> shares = (List<ShareDTO>) wrapper.get();
                    return shares != null ? shares.size() : 0;
                }
            }
        } catch (Exception e) {
            logger.debug("Ошибка при получении количества акций из кэша: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Получает количество фьючерсов из кэша
     *
     * @return количество фьючерсов в кэше
     */
    private int getFuturesCountFromCache() {
        try {
            Cache futuresCache = cacheManager.getCache("futuresCache");
            if (futuresCache != null) {
                Cache.ValueWrapper wrapper = futuresCache.get("all_futures");
                if (wrapper != null && wrapper.get() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<FutureDTO> futures = (List<FutureDTO>) wrapper.get();
                    return futures != null ? futures.size() : 0;
                }
            }
        } catch (Exception e) {
            logger.debug("Ошибка при получении количества фьючерсов из кэша: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Получает количество индикативов из кэша
     *
     * @return количество индикативов в кэше
     */
    private int getIndicativesCountFromCache() {
        try {
            Cache indicativesCache = cacheManager.getCache("indicativesCache");
            if (indicativesCache != null) {
                Cache.ValueWrapper wrapper = indicativesCache.get("all_indicatives");
                if (wrapper != null && wrapper.get() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<IndicativeDTO> indicatives = (List<IndicativeDTO>) wrapper.get();
                    return indicatives != null ? indicatives.size() : 0;
                }
            }
        } catch (Exception e) {
            logger.debug("Ошибка при получении количества индикативов из кэша: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Получает статистику кэша в формате JSON
     *
     * <p>
     * Возвращает структурированную информацию о состоянии кэшей в JSON формате
     * для удобного использования в API и мониторинге.
     * </p>
     *
     * @return Map с детальной статистикой кэшей
     */
    public Map<String, Object> getCacheStatisticsJson() {
        Map<String, Object> statistics = new HashMap<>();
        Map<String, Object> cacheStats = new HashMap<>();

        try {
            statistics.put("timestamp", LocalDateTime.now(TimeZoneUtils.getMoscowZone()).toString());
            statistics.put("total_caches", 3);
            statistics.put("cache_type", "Caffeine Cache");
            statistics.put("configuration", "maximumSize=1000, expireAfterWrite=10m, expireAfterAccess=5m");

            // Статистика кэша акций
            Map<String, Object> sharesStats = getCacheStats("sharesCache");
            cacheStats.put("shares_cache", sharesStats);

            // Статистика кэша фьючерсов
            Map<String, Object> futuresStats = getCacheStats("futuresCache");
            cacheStats.put("futures_cache", futuresStats);

            // Статистика кэша индикативов
            Map<String, Object> indicativesStats = getCacheStats("indicativesCache");
            cacheStats.put("indicatives_cache", indicativesStats);

            statistics.put("caches", cacheStats);

            // Общая сводка по инструментам
            int totalShares = getSharesCountFromCache();
            int totalFutures = getFuturesCountFromCache();
            int totalIndicatives = getIndicativesCountFromCache();
            int totalInstruments = totalShares + totalFutures + totalIndicatives;

            Map<String, Object> instrumentsSummary = new HashMap<>();
            instrumentsSummary.put("shares_count", totalShares);
            instrumentsSummary.put("futures_count", totalFutures);
            instrumentsSummary.put("indicatives_count", totalIndicatives);
            instrumentsSummary.put("total_instruments", totalInstruments);

            statistics.put("instruments_summary", instrumentsSummary);

        } catch (Exception e) {
            logger.error("Ошибка при получении JSON статистики кэша: {}", e.getMessage(), e);
            statistics.put("error", "Ошибка при получении статистики кэша: " + e.getMessage());
        }

        return statistics;
    }

    /**
     * Получает статистику конкретного кэша
     *
     * @param cacheName имя кэша
     * @return Map со статистикой кэша
     */
    private Map<String, Object> getCacheStats(String cacheName) {
        Map<String, Object> stats = new HashMap<>();

        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Object nativeCache = cache.getNativeCache();
                if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                    @SuppressWarnings("rawtypes")
                    com.github.benmanes.caffeine.cache.Cache caffeineCache = (com.github.benmanes.caffeine.cache.Cache) nativeCache;

                    stats.put("exists", true);
                    stats.put("size", caffeineCache.estimatedSize());
                    stats.put("hit_rate", String.format("%.2f%%", caffeineCache.stats().hitRate() * 100));
                    stats.put("miss_rate", String.format("%.2f%%", caffeineCache.stats().missRate() * 100));
                    stats.put("eviction_count", caffeineCache.stats().evictionCount());
                    stats.put("hit_count", caffeineCache.stats().hitCount());
                    stats.put("miss_count", caffeineCache.stats().missCount());
                    stats.put("load_count", caffeineCache.stats().loadCount());
                    stats.put("load_success_count", caffeineCache.stats().loadSuccessCount());
                    stats.put("load_failure_count", caffeineCache.stats().loadFailureCount());

                    // Ключи в кэше
                    stats.put("keys", caffeineCache.asMap().keySet());

                    // Общее время загрузки
                    stats.put("total_load_time_ns", caffeineCache.stats().totalLoadTime());

                    // Количество инструментов в кэше
                    int instrumentsCount = 0;
                    if ("sharesCache".equals(cacheName)) {
                        instrumentsCount = getSharesCountFromCache();
                    } else if ("futuresCache".equals(cacheName)) {
                        instrumentsCount = getFuturesCountFromCache();
                    } else if ("indicativesCache".equals(cacheName)) {
                        instrumentsCount = getIndicativesCountFromCache();
                    }
                    stats.put("instruments_count", instrumentsCount);

                } else {
                    stats.put("exists", true);
                    stats.put("error", "Нативный кэш недоступен");
                }
            } else {
                stats.put("exists", false);
                stats.put("error", "Кэш не найден");
            }
        } catch (Exception e) {
            stats.put("exists", false);
            stats.put("error", "Ошибка при получении статистики: " + e.getMessage());
        }

        return stats;
    }
}
