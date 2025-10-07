package com.example.investmenttradingservice.service;

import java.util.ArrayList;
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
import com.example.investmenttradingservice.Entity.FutureEntity;
import com.example.investmenttradingservice.Entity.ShareEntity;
import com.example.investmenttradingservice.Entity.IndicativeEntity;
import com.example.investmenttradingservice.mapper.Mapper;
import com.example.investmenttradingservice.repository.FutureRepository;
import com.example.investmenttradingservice.repository.ShareRepository;
import com.example.investmenttradingservice.repository.Indicativerepository;

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
     * @param cacheManager         менеджер кэша
     */
    public CacheInstrumentsService(ShareRepository shareRepository, FutureRepository futureRepository,
            Indicativerepository indicativeRepository, CacheManager cacheManager) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.cacheManager = cacheManager;
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
        try {
            List<ShareEntity> cachedShares = getShareFromCache();
            if (cachedShares != null && !cachedShares.isEmpty()) {
                logger.info("Получено {} акций из кэша", cachedShares.size());
                return mapper.toShareDTOList(cachedShares);
            }

            // Fallback на БД
            logger.info("Кэш акций пуст, загружаем из БД");
            return mapper.toShareDTOList(shareRepository.findAll());

        } catch (Exception e) {
            logger.error("Ошибка при получении акций из кэша, используем БД: {}", e.getMessage(), e);
            return mapper.toShareDTOList(shareRepository.findAll());
        }

    }

    /**
     * Получает акции из кэша sharesCache
     *
     * <p>
     * Выполняет поиск акций в кэше по различным возможным ключам.
     * Если стандартные ключи не найдены, пытается получить все записи
     * из кэша через нативный интерфейс Caffeine.
     * </p>
     *
     * <p>
     * Алгоритм поиска:
     * </p>
     * <ul>
     * <li>Проверка стандартных ключей кэша (all_shares, |||, и др.)</li>
     * <li>При отсутствии данных - поиск через нативный кэш</li>
     * <li>Преобразование найденных DTO в Entity</li>
     * </ul>
     *
     * @return список акций в формате ShareEntity или пустой список
     */
    private List<ShareEntity> getShareFromCache() {
        Cache cache = cacheManager.getCache("sharesCache");
        if (cache == null) {
            logger.warn("Кэш sharesCache не найден");
            return Collections.emptyList();
        }
        List<ShareDTO> allShares = new ArrayList<>();

        // Получаем все записи из кэша через ключи
        try {
            // Пробуем разные ключи для получения акций
            String[] possibleKeys = {
                    "all_shares", // Основной ключ из CacheService
                    "|||", // Пустые параметры
                    "|moex_mrng_evng_e_wknd_dlr|||", // Ключ из CacheController
            };

            logger.debug("Поиск акций в кэше по ключам: {}", String.join(", ", possibleKeys));

            for (String cacheKey : possibleKeys) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null && wrapper.get() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<ShareDTO> shares = (List<ShareDTO>) wrapper.get();
                    if (shares != null && !shares.isEmpty()) {
                        allShares.addAll(shares);
                        logger.info("Найдено {} акций в кэше с ключом: {}", shares.size(), cacheKey);
                        break; // Используем первый найденный ключ
                    }
                } else {
                    logger.debug("Ключ '{}' не найден в кэше или содержит неверный тип данных", cacheKey);
                }
            }

            // Если не нашли по стандартным ключам, пробуем получить все записи из кэша
            if (allShares.isEmpty() && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                logger.debug("Поиск акций через нативный интерфейс Caffeine");
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache
                        .getNativeCache();

                for (Map.Entry<?, ?> entry : caffeineCache.asMap().entrySet()) {
                    logger.debug("Проверка ключа в нативном кэше: {}", entry.getKey());
                    if (entry.getValue() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<ShareDTO> shares = (List<ShareDTO>) entry.getValue();
                        if (shares != null && !shares.isEmpty()) {
                            allShares.addAll(shares);
                            logger.info("Найдено {} акций в нативном кэше с ключом: {}", shares.size(), entry.getKey());
                            break; // Используем первый найденный список
                        }
                    }
                }
            }

            if (allShares.isEmpty()) {
                logger.warn("Акции не найдены в кэше ни по одному из ключей");
            }

        } catch (Exception e) {
            logger.error("Ошибка получения акций из кэша: {}", e.getMessage(), e);
        }

        return mapper.toShareEntityList(allShares);
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
        try {
            List<FutureEntity> cachedFutures = getFutureFromCache();
            if (cachedFutures != null && !cachedFutures.isEmpty()) {
                logger.info("Получено {} фьючерсов из кэша", cachedFutures.size());
                return mapper.toFutureDTOList(cachedFutures);
            }

            // Fallback на БД
            logger.info("Кэш фьючерсов пуст, загружаем из БД");
            return mapper.toFutureDTOList(futureRepository.findAll());

        } catch (Exception e) {
            logger.error("Ошибка при получении фьючерсов из кэша, используем БД: {}", e.getMessage(), e);
            return mapper.toFutureDTOList(futureRepository.findAll());
        }
    }

    /**
     * Получает фьючерсы из кэша futuresCache
     *
     * <p>
     * Выполняет поиск фьючерсов в кэше по различным возможным ключам.
     * Если стандартные ключи не найдены, пытается получить все записи
     * из кэша через нативный интерфейс Caffeine.
     * </p>
     *
     * <p>
     * Алгоритм поиска:
     * </p>
     * <ul>
     * <li>Проверка стандартных ключей кэша</li>
     * <li>При отсутствии данных - поиск через нативный кэш</li>
     * <li>Преобразование найденных DTO в Entity</li>
     * </ul>
     *
     * @return список фьючерсов в формате FutureEntity или пустой список
     */
    private List<FutureEntity> getFutureFromCache() {
        Cache cache = cacheManager.getCache("futuresCache");
        if (cache == null) {
            logger.warn("Кэш futuresCache не найден");
            return new ArrayList<>();
        }

        List<FutureDTO> allFutures = new ArrayList<>();

        // Получаем все записи из кэша через ключи
        try {
            // Пробуем разные ключи для получения фьючерсов
            String[] possibleKeys = {
                    "all_futures", // Основной ключ из CacheService
                    "||||", // Пустые параметры
            };

            logger.debug("Поиск фьючерсов в кэше по ключам: {}", String.join(", ", possibleKeys));

            for (String cacheKey : possibleKeys) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null && wrapper.get() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<FutureDTO> futures = (List<FutureDTO>) wrapper.get();
                    if (futures != null && !futures.isEmpty()) {
                        allFutures.addAll(futures);
                        logger.info("Найдено {} фьючерсов в кэше с ключом: {}", futures.size(), cacheKey);
                        break; // Используем первый найденный ключ
                    }
                } else {
                    logger.debug("Ключ '{}' не найден в кэше или содержит неверный тип данных", cacheKey);
                }
            }

            // Если не нашли по стандартным ключам, пробуем получить все записи из кэша
            if (allFutures.isEmpty() && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                logger.debug("Поиск фьючерсов через нативный интерфейс Caffeine");
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache
                        .getNativeCache();

                for (Map.Entry<?, ?> entry : caffeineCache.asMap().entrySet()) {
                    logger.debug("Проверка ключа в нативном кэше: {}", entry.getKey());
                    if (entry.getValue() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<FutureDTO> futures = (List<FutureDTO>) entry.getValue();
                        if (futures != null && !futures.isEmpty()) {
                            allFutures.addAll(futures);
                            logger.info("Найдено {} фьючерсов в нативном кэше с ключом: {}", futures.size(),
                                    entry.getKey());
                            break; // Используем первый найденный список
                        }
                    }
                }
            }

            if (allFutures.isEmpty()) {
                logger.warn("Фьючерсы не найдены в кэше ни по одному из ключей");
            }

        } catch (Exception e) {
            logger.error("Ошибка получения фьючерсов из кэша: {}", e.getMessage(), e);
        }

        return mapper.toFutureEntityList(allFutures);
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
        try {
            List<IndicativeEntity> cachedIndicatives = getIndicativeFromCache();
            if (cachedIndicatives != null && !cachedIndicatives.isEmpty()) {
                logger.info("Получено {} индикативов из кэша", cachedIndicatives.size());
                return mapper.toIndicativeDTOList(cachedIndicatives);
            }

            // Fallback на БД
            logger.info("Кэш индикативов пуст, загружаем из БД");
            return mapper.toIndicativeDTOList(indicativeRepository.findAll());

        } catch (Exception e) {
            logger.error("Ошибка при получении индикативов из кэша, используем БД: {}", e.getMessage(), e);
            return mapper.toIndicativeDTOList(indicativeRepository.findAll());
        }
    }

    /**
     * Получает индикативы из кэша indicativesCache
     *
     * <p>
     * Выполняет поиск индикативов в кэше по различным возможным ключам.
     * Если стандартные ключи не найдены, пытается получить все записи
     * из кэша через нативный интерфейс Caffeine.
     * </p>
     *
     * <p>
     * Алгоритм поиска:
     * </p>
     * <ul>
     * <li>Проверка стандартных ключей кэша</li>
     * <li>При отсутствии данных - поиск через нативный кэш</li>
     * <li>Преобразование найденных DTO в Entity</li>
     * </ul>
     *
     * @return список индикативов в формате IndicativeEntity или пустой список
     */
    private List<IndicativeEntity> getIndicativeFromCache() {
        Cache cache = cacheManager.getCache("indicativesCache");
        if (cache == null) {
            logger.warn("Кэш indicativesCache не найден");
            return Collections.emptyList();
        }

        List<IndicativeDTO> allIndicatives = new ArrayList<>();

        // Получаем все записи из кэша через ключи
        try {
            // Пробуем разные ключи для получения индикативов
            String[] possibleKeys = {
                    "all_indicatives", // Основной ключ из CacheService
                    "|||||", // Пустые параметры
            };

            logger.debug("Поиск индикативов в кэше по ключам: {}", String.join(", ", possibleKeys));

            for (String cacheKey : possibleKeys) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null && wrapper.get() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<IndicativeDTO> indicatives = (List<IndicativeDTO>) wrapper.get();
                    if (indicatives != null && !indicatives.isEmpty()) {
                        allIndicatives.addAll(indicatives);
                        logger.info("Найдено {} индикативов в кэше с ключом: {}", indicatives.size(), cacheKey);
                        break; // Используем первый найденный ключ
                    }
                } else {
                    logger.debug("Ключ '{}' не найден в кэше или содержит неверный тип данных", cacheKey);
                }
            }

            // Если не нашли по стандартным ключам, пробуем получить все записи из кэша
            if (allIndicatives.isEmpty()
                    && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                logger.debug("Поиск индикативов через нативный интерфейс Caffeine");
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache
                        .getNativeCache();

                for (Map.Entry<?, ?> entry : caffeineCache.asMap().entrySet()) {
                    logger.debug("Проверка ключа в нативном кэше: {}", entry.getKey());
                    if (entry.getValue() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<IndicativeDTO> indicatives = (List<IndicativeDTO>) entry.getValue();
                        if (indicatives != null && !indicatives.isEmpty()) {
                            allIndicatives.addAll(indicatives);
                            logger.info("Найдено {} индикативов в нативном кэше с ключом: {}", indicatives.size(),
                                    entry.getKey());
                            break; // Используем первый найденный список
                        }
                    }
                }
            }

            if (allIndicatives.isEmpty()) {
                logger.warn("Индикативы не найдены в кэше ни по одному из ключей");
            }

        } catch (Exception e) {
            logger.error("Ошибка получения индикативов из кэша: {}", e.getMessage(), e);
        }

        return mapper.toIndicativeEntityList(allIndicatives);
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
        try {
            List<ShareEntity> cachedShares = getShareFromCache();
            if (cachedShares != null && !cachedShares.isEmpty()) {
                logger.info("Получено {} акций из кэша (только кэш)", cachedShares.size());
                return mapper.toShareDTOList(cachedShares);
            } else {
                logger.info("Кэш акций пуст, возвращаем пустой список");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении акций только из кэша: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
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
        try {
            List<FutureEntity> cachedFutures = getFutureFromCache();
            if (cachedFutures != null && !cachedFutures.isEmpty()) {
                logger.info("Получено {} фьючерсов из кэша (только кэш)", cachedFutures.size());
                return mapper.toFutureDTOList(cachedFutures);
            } else {
                logger.info("Кэш фьючерсов пуст, возвращаем пустой список");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении фьючерсов только из кэша: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
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
        try {
            List<IndicativeEntity> cachedIndicatives = getIndicativeFromCache();
            if (cachedIndicatives != null && !cachedIndicatives.isEmpty()) {
                logger.info("Получено {} индикативов из кэша (только кэш)", cachedIndicatives.size());
                return mapper.toIndicativeDTOList(cachedIndicatives);
            } else {
                logger.info("Кэш индикативов пуст, возвращаем пустой список");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении индикативов только из кэша: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
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
     * @return Map с тремя списками: shares, futures, indicatives
     */
    public Map<String, Object> getAllInstrumentsFromCacheOnly() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ShareDTO> shares = getSharesFromCacheOnly();
            List<FutureDTO> futures = getFuturesFromCacheOnly();
            List<IndicativeDTO> indicatives = getIndicativesFromCacheOnly();

            result.put("shares", shares);
            result.put("futures", futures);
            result.put("indicatives", indicatives);
            result.put("shares_size", shares.size());
            result.put("futures_size", futures.size());
            result.put("indicatives_size", indicatives.size());
            result.put("total_instruments", shares.size() + futures.size() + indicatives.size());

            logger.info("Получены инструменты только из кэша: {} акций, {} фьючерсов, {} индикативов",
                    shares.size(), futures.size(), indicatives.size());

        } catch (Exception e) {
            logger.error("Ошибка при получении всех инструментов только из кэша: {}", e.getMessage(), e);
            result.put("shares", new ArrayList<>());
            result.put("futures", new ArrayList<>());
            result.put("indicatives", new ArrayList<>());
            result.put("shares_size", 0);
            result.put("futures_size", 0);
            result.put("indicatives_size", 0);
            result.put("total_instruments", 0);
        }

        return result;
    }
}
