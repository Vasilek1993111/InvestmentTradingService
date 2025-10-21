package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

import com.example.investmenttradingservice.DTO.FutureDTO;
import com.example.investmenttradingservice.DTO.LimitsDto;
import com.example.investmenttradingservice.DTO.ShareDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.service.TInvestApiService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис асинхронного получения лимитов для инструментов и кэширования
 * результата
 *
 * <p>
 * Берёт все акции и фьючерсы из кэша, параллельно вызывает
 * {@link TInvestApiService#getLimitsForInstrument(String)} для каждого FIGI и
 * складывает итоговый список {@link LimitsDto} в кэш `limitsCache` под ключом
 * `all_limits`.
 * </p>
 */
@Service
public class LimitService extends AbstractCacheableInstrumentService<LimitsDto> {

    private static final Logger log = LoggerFactory.getLogger(LimitService.class);
    private static final String LIMITS_CACHE_NAME = InstrumentType.LIMITS.getCacheName();
    private static final String LIMITS_CACHE_KEY = "all_limits";

    private final TInvestApiService tInvestApiService;
    private final ShareService shareService;
    private final FutureService futureService;

    private final ExecutorService executor;

    public LimitService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            TInvestApiService tInvestApiService,
            ShareService shareService,
            FutureService futureService) {
        super(cacheManager, instrumentCacheManager, InstrumentType.LIMITS);
        this.tInvestApiService = tInvestApiService;
        this.shareService = shareService;
        this.futureService = futureService;
        // Ограниченный пул для контроля нагрузки и соблюдения rate limits
        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        this.executor = Executors.newFixedThreadPool(threads);
    }

    /**
     * Запускает актуализацию лимитов по всем акциям и фьючерсам из кэша
     * с задержкой и retry-механизмом для соблюдения rate limits API.
     *
     * @return количество успешно обновлённых лимитов
     */
    public int refreshAllLimits() {
        List<String> instrumentIds = collectAllInstrumentIdsFromCache();
        if (instrumentIds.isEmpty()) {
            log.warn("Кэш акций/фьючерсов пуст, пропускаем обновление лимитов");
            putLimitsToCache(Collections.emptyList());
            return 0;
        }

        log.info("Начинаем обновление лимитов для {} инструментов", instrumentIds.size());
        List<LimitsDto> allLimits = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < instrumentIds.size(); i++) {
            String instrumentId = instrumentIds.get(i);

            try {
                LimitsDto limits = getLimitsWithRetry(instrumentId);
                allLimits.add(limits);
                successCount++;

                log.debug("Получены лимиты для {}: limitDown={}, limitUp={}",
                        instrumentId, limits.limitDown(), limits.limitUp());

                // Задержка между запросами для соблюдения rate limits
                if (i < instrumentIds.size() - 1) { // Не делаем задержку после последнего запроса
                    Thread.sleep(100); // 100ms задержка
                }

            } catch (Exception ex) {
                log.error("Ошибка при получении лимитов для {}: {}", instrumentId, ex.getMessage());
                allLimits.add(new LimitsDto(instrumentId, null, null));
                errorCount++;
            }
        }

        putLimitsToCache(allLimits);
        log.info("Обновление лимитов завершено: успешно={}, ошибок={}, всего={}",
                successCount, errorCount, allLimits.size());

        return successCount;
    }

    /**
     * Получает лимиты для инструмента с retry-механизмом.
     *
     * @param instrumentId идентификатор инструмента
     * @return лимиты инструмента
     * @throws Exception если не удалось получить лимиты после всех попыток
     */
    private LimitsDto getLimitsWithRetry(String instrumentId) throws Exception {
        int maxRetries = 3;
        long baseDelayMs = 200L; // Базовая задержка 200ms

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return tInvestApiService.getLimitsForInstrument(instrumentId);

            } catch (Exception ex) {
                if (attempt == maxRetries) {
                    log.error("Не удалось получить лимиты для {} после {} попыток: {}",
                            instrumentId, maxRetries, ex.getMessage());
                    throw ex;
                }

                long delayMs = baseDelayMs * attempt; // Экспоненциальная задержка: 200ms, 400ms, 600ms
                log.warn("Попытка {} из {} не удалась для {}, повтор через {}ms: {}",
                        attempt, maxRetries, instrumentId, delayMs, ex.getMessage());

                Thread.sleep(delayMs);
            }
        }

        throw new RuntimeException("Неожиданная ошибка в retry-механизме");
    }

    /**
     * Возвращает текущие лимиты из кэша.
     */
    public List<LimitsDto> getLimitsFromCache() {
        Cache cache = cacheManager.getCache(LIMITS_CACHE_NAME);
        if (cache == null) {
            return Collections.emptyList();
        }
        Cache.ValueWrapper wrapper = cache.get(LIMITS_CACHE_KEY);
        if (wrapper == null || !(wrapper.get() instanceof List<?> list)) {
            return Collections.emptyList();
        }
        @SuppressWarnings("unchecked")
        List<LimitsDto> limits = (List<LimitsDto>) list;
        return limits;
    }

    /**
     * Ищет лимит по FIGI/InstrumentId в кэше.
     */
    public LimitsDto getLimitByInstrumentId(String instrumentId) {
        if (instrumentId == null || instrumentId.isBlank()) {
            return null;
        }
        return getLimitsFromCache().stream()
                .filter(l -> instrumentId.equals(l.instrumentId()))
                .findFirst()
                .orElse(null);
    }

    private List<String> collectAllInstrumentIdsFromCache() {
        List<ShareDTO> shares = shareService.getFromCacheOnly();
        List<FutureDTO> futures = futureService.getFromCacheOnly();
        List<String> shareIds = shares == null ? List.of() : shares.stream().map(ShareDTO::figi).toList();
        List<String> futureIds = futures == null ? List.of() : futures.stream().map(FutureDTO::figi).toList();
        return Collections.unmodifiableList(
                new ArrayList<>(
                        Set.copyOf(new ArrayList<String>() {
                            private static final long serialVersionUID = 1L;
                            {
                                addAll(shareIds);
                                addAll(futureIds);
                            }
                        })));
    }

    private void putLimitsToCache(List<LimitsDto> limits) {
        Cache cache = cacheManager.getCache(LIMITS_CACHE_NAME);
        if (cache != null) {
            cache.put(LIMITS_CACHE_KEY, limits);
            log.info("В кэш {} записано лимитов: {}", LIMITS_CACHE_NAME, limits.size());
        }
    }

    @Override
    protected String getFigiFromItem(LimitsDto item) {
        return item.instrumentId();
    }

    @PreDestroy
    private void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
