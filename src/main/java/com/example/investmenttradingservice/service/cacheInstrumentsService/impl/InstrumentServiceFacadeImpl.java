package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.*;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.service.InstrumentServiceFacade;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Реализация фасада для работы со всеми типами инструментов
 *
 * <p>
 * Предоставляет единую точку доступа ко всем сервисам инструментов.
 * Реализует паттерн Facade для упрощения взаимодействия с множественными
 * сервисами инструментов. Скрывает сложность системы и предоставляет
 * упрощенный API для клиентов.
 * </p>
 *
 * <p>
 * Архитектурные преимущества:
 * </p>
 * <ul>
 * <li>Единая точка входа для всех операций с инструментами</li>
 * <li>Упрощение API для клиентов</li>
 * <li>Возможность добавления новой логики без изменения клиентов</li>
 * <li>Централизованная обработка ошибок и логирование</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class InstrumentServiceFacadeImpl implements InstrumentServiceFacade {

    /** Логгер для записи операций фасада */
    private static final Logger logger = LoggerFactory.getLogger(InstrumentServiceFacadeImpl.class);

    /** Сервис для работы с акциями */
    private final ShareService shareService;

    /** Сервис для работы с фьючерсами */
    private final FutureService futureService;

    /** Сервис для работы с индикативами */
    private final IndicativeService indicativeService;

    /** Сервис для работы с ценами закрытия */
    private final ClosePriceService closePriceService;

    /** Сервис для работы с ценами открытия */
    private final OpenPriceService openPriceService;

    /** Сервис для работы с ценами закрытия вечерней сессии */
    private final ClosePriceEveningSessionService closePriceEveningSessionService;

    /** Сервис для работы с ценами последних сделок */
    private final LastPriceService lastPriceService;

    /** Сервис для работы с дивидендами */
    private final DividendService dividendService;

    /** Менеджер кэша инструментов */
    private final InstrumentCacheManager instrumentCacheManager;

    /**
     * Конструктор фасада сервисов инструментов
     *
     * @param shareService                    сервис для работы с акциями
     * @param futureService                   сервис для работы с фьючерсами
     * @param indicativeService               сервис для работы с индикативами
     * @param closePriceService               сервис для работы с ценами закрытия
     * @param openPriceService                сервис для работы с ценами открытия
     * @param closePriceEveningSessionService сервис для работы с ценами закрытия
     *                                        вечерней сессии
     * @param lastPriceService                сервис для работы с ценами последних
     *                                        сделок
     * @param dividendService                 сервис для работы с дивидендами
     * @param instrumentCacheManager          менеджер кэша инструментов
     */
    public InstrumentServiceFacadeImpl(ShareService shareService,
            FutureService futureService,
            IndicativeService indicativeService,
            ClosePriceService closePriceService,
            OpenPriceService openPriceService,
            ClosePriceEveningSessionService closePriceEveningSessionService,
            LastPriceService lastPriceService,
            DividendService dividendService,
            InstrumentCacheManager instrumentCacheManager) {
        this.shareService = shareService;
        this.futureService = futureService;
        this.indicativeService = indicativeService;
        this.closePriceService = closePriceService;
        this.openPriceService = openPriceService;
        this.closePriceEveningSessionService = closePriceEveningSessionService;
        this.lastPriceService = lastPriceService;
        this.dividendService = dividendService;
        this.instrumentCacheManager = instrumentCacheManager;
    }

    // ===========================================
    // Методы для получения всех инструментов
    // ===========================================

    /**
     * Получает все инструменты из кэша с fallback на БД
     *
     * @return Map с инструментами и их размерами
     */
    @Override
    public Map<String, Object> getAllInstruments() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Получаем данные для всех типов инструментов
            List<ShareDTO> shares = shareService.getAll();
            List<FutureDTO> futures = futureService.getAll();
            List<IndicativeDTO> indicatives = indicativeService.getAll();
            List<ClosePriceDTO> closePrices = closePriceService.getAll();
            List<OpenPriceDTO> openPrices = openPriceService.getAll();
            List<ClosePriceEveningSessionDTO> closePriceEveningSessions = closePriceEveningSessionService.getAll();
            List<LastPriceDTO> lastPrices = lastPriceService.getAll();
            List<DividendDto> dividends = dividendService.getAll();

            // Формируем результат
            buildResultMap(result, shares, futures, indicatives, closePrices, openPrices,
                    closePriceEveningSessions, lastPrices, dividends);

            logger.info(
                    "Получены все инструменты: {} акций, {} фьючерсов, {} индикативов, {} цен закрытия, {} цен открытия, {} цен последних сделок, {} дивидендов",
                    shares.size(), futures.size(), indicatives.size(), closePrices.size(), openPrices.size(),
                    lastPrices.size(), dividends.size());

        } catch (Exception e) {
            logger.error("Ошибка при получении всех инструментов: {}", e.getMessage(), e);
            buildEmptyResult(result);
        }

        return result;
    }

    /**
     * Получает все инструменты ТОЛЬКО из кэша (без fallback на БД)
     *
     * @return Map с инструментами и их размерами
     */
    @Override
    public Map<String, Object> getAllInstrumentsFromCacheOnly() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Получаем данные только из кэша для всех типов инструментов
            List<ShareDTO> shares = shareService.getFromCacheOnly();
            List<FutureDTO> futures = futureService.getFromCacheOnly();
            List<IndicativeDTO> indicatives = indicativeService.getFromCacheOnly();
            List<ClosePriceDTO> closePrices = closePriceService.getFromCacheOnly();
            List<OpenPriceDTO> openPrices = openPriceService.getFromCacheOnly();
            List<ClosePriceEveningSessionDTO> closePriceEveningSessions = closePriceEveningSessionService
                    .getFromCacheOnly();
            List<LastPriceDTO> lastPrices = lastPriceService.getFromCacheOnly();
            List<DividendDto> dividends = dividendService.getFromCacheOnly();

            // Формируем результат
            buildResultMap(result, shares, futures, indicatives, closePrices, openPrices,
                    closePriceEveningSessions, lastPrices, dividends);

            logger.info(
                    "Получены инструменты только из кэша: {} акций, {} фьючерсов, {} индикативов, {} цен закрытия, {} цен открытия, {} цен последних сделок, {} дивидендов",
                    shares.size(), futures.size(), indicatives.size(), closePrices.size(), openPrices.size(),
                    lastPrices.size(), dividends.size());

        } catch (Exception e) {
            logger.error("Ошибка при получении всех инструментов только из кэша: {}", e.getMessage(), e);
            buildEmptyResult(result);
        }

        return result;
    }

    // ===========================================
    // Методы для получения конкретных инструментов
    // ===========================================

    @Override
    public List<ShareDTO> getShares() {
        return shareService.getAll();
    }

    @Override
    public List<FutureDTO> getFutures() {
        return futureService.getAll();
    }

    @Override
    public List<IndicativeDTO> getIndicatives() {
        return indicativeService.getAll();
    }

    @Override
    public List<ClosePriceDTO> getClosePrices() {
        return closePriceService.getAll();
    }

    @Override
    public List<OpenPriceDTO> getOpenPrices() {
        return openPriceService.getAll();
    }

    @Override
    public List<ClosePriceEveningSessionDTO> getClosePriceEveningSessions() {
        return closePriceEveningSessionService.getAll();
    }

    @Override
    public List<LastPriceDTO> getLastPrices() {
        return lastPriceService.getAll();
    }

    @Override
    public List<DividendDto> getDividends() {
        return dividendService.getAll();
    }

    // ===========================================
    // Методы для получения инструментов только из кэша
    // ===========================================

    @Override
    public List<ShareDTO> getSharesFromCacheOnly() {
        return shareService.getFromCacheOnly();
    }

    @Override
    public List<FutureDTO> getFuturesFromCacheOnly() {
        return futureService.getFromCacheOnly();
    }

    @Override
    public List<IndicativeDTO> getIndicativesFromCacheOnly() {
        return indicativeService.getFromCacheOnly();
    }

    @Override
    public List<ClosePriceDTO> getClosePricesFromCacheOnly() {
        return closePriceService.getFromCacheOnly();
    }

    @Override
    public List<OpenPriceDTO> getOpenPricesFromCacheOnly() {
        return openPriceService.getFromCacheOnly();
    }

    @Override
    public List<ClosePriceEveningSessionDTO> getClosePriceEveningSessionsFromCacheOnly() {
        return closePriceEveningSessionService.getFromCacheOnly();
    }

    @Override
    public List<LastPriceDTO> getLastPricesFromCacheOnly() {
        return lastPriceService.getFromCacheOnly();
    }

    @Override
    public List<DividendDto> getDividendsFromCacheOnly() {
        return dividendService.getFromCacheOnly();
    }

    // ===========================================
    // Методы для поиска по FIGI
    // ===========================================

    /**
     * Получает информацию об инструменте по FIGI из всех источников
     *
     * @param figi идентификатор инструмента
     * @return список всех найденных записей
     */
    @Override
    public List<Object> getInstrumentByFigi(String figi) {
        if (figi == null || figi.trim().isEmpty()) {
            logger.warn("FIGI не может быть пустым");
            return new ArrayList<>();
        }

        List<Object> allResults = new ArrayList<>();
        String trimmedFigi = figi.trim();

        logger.info("Поиск инструмента по FIGI: {}", trimmedFigi);

        try {
            // Поиск во всех типах инструментов
            allResults.addAll(shareService.getByFigi(trimmedFigi));
            allResults.addAll(futureService.getByFigi(trimmedFigi));
            allResults.addAll(indicativeService.getByFigi(trimmedFigi));
            allResults.addAll(closePriceService.getByFigi(trimmedFigi));
            allResults.addAll(openPriceService.getByFigi(trimmedFigi));
            allResults.addAll(lastPriceService.getByFigi(trimmedFigi));
            allResults.addAll(dividendService.getByFigi(trimmedFigi));

            logger.info("Найдено {} записей для FIGI: {}", allResults.size(), trimmedFigi);

        } catch (Exception e) {
            logger.error("Ошибка при поиске инструмента по FIGI {}: {}", trimmedFigi, e.getMessage(), e);
        }

        return allResults;
    }

    /**
     * Получает информацию об инструменте по FIGI только из кэша
     *
     * @param figi идентификатор инструмента
     * @return список всех найденных записей из кэша
     */
    @Override
    public List<Object> getInstrumentByFigiFromCacheOnly(String figi) {
        if (figi == null || figi.trim().isEmpty()) {
            logger.warn("FIGI не может быть пустым");
            return new ArrayList<>();
        }

        List<Object> allResults = new ArrayList<>();
        String trimmedFigi = figi.trim();

        logger.info("Поиск инструмента по FIGI только в кэше: {}", trimmedFigi);

        try {
            // Поиск только в кэшированных данных
            allResults.addAll(shareService.getByFigiFromCacheOnly(trimmedFigi));
            allResults.addAll(futureService.getByFigiFromCacheOnly(trimmedFigi));
            allResults.addAll(indicativeService.getByFigiFromCacheOnly(trimmedFigi));
            allResults.addAll(closePriceService.getByFigiFromCacheOnly(trimmedFigi));
            allResults.addAll(openPriceService.getByFigiFromCacheOnly(trimmedFigi));
            allResults.addAll(lastPriceService.getByFigiFromCacheOnly(trimmedFigi));
            allResults.addAll(dividendService.getByFigiFromCacheOnly(trimmedFigi));

            logger.info("Найдено {} записей в кэше для FIGI: {}", allResults.size(), trimmedFigi);

        } catch (Exception e) {
            logger.error("Ошибка при поиске инструмента по FIGI в кэше {}: {}", trimmedFigi, e.getMessage(), e);
        }

        return allResults;
    }

    // ===========================================
    // Методы для получения статистики
    // ===========================================

    /**
     * Получает статистику по всем кэшам
     *
     * @return Map со статистикой кэшей
     */
    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // Получаем статистику от менеджера кэша
            Map<String, Object> cacheStats = instrumentCacheManager.getCacheStatistics();
            statistics.put("cacheManager", cacheStats);

            // Получаем размеры кэшей от сервисов
            Map<String, Integer> serviceCacheSizes = new HashMap<>();
            serviceCacheSizes.put("shares", shareService.getCacheSize());
            serviceCacheSizes.put("futures", futureService.getCacheSize());
            serviceCacheSizes.put("indicatives", indicativeService.getCacheSize());
            serviceCacheSizes.put("closePrices", closePriceService.getCacheSize());
            serviceCacheSizes.put("openPrices", openPriceService.getCacheSize());
            serviceCacheSizes.put("closePriceEveningSessions", closePriceEveningSessionService.getCacheSize());
            serviceCacheSizes.put("lastPrices", lastPriceService.getCacheSize());
            serviceCacheSizes.put("dividends", dividendService.getCacheSize());

            statistics.put("serviceCacheSizes", serviceCacheSizes);

            // Получаем размеры БД от сервисов
            Map<String, Integer> databaseSizes = new HashMap<>();
            databaseSizes.put("shares", shareService.getDatabaseSize());
            databaseSizes.put("futures", futureService.getDatabaseSize());
            databaseSizes.put("indicatives", indicativeService.getDatabaseSize());
            databaseSizes.put("closePrices", closePriceService.getDatabaseSize());
            databaseSizes.put("openPrices", openPriceService.getDatabaseSize());
            databaseSizes.put("closePriceEveningSessions", closePriceEveningSessionService.getDatabaseSize());
            databaseSizes.put("lastPrices", lastPriceService.getDatabaseSize());
            databaseSizes.put("dividends", dividendService.getDatabaseSize());

            statistics.put("databaseSizes", databaseSizes);

            logger.info("Получена статистика по кэшам и БД");

        } catch (Exception e) {
            logger.error("Ошибка при получении статистики: {}", e.getMessage(), e);
            statistics.put("error", e.getMessage());
        }

        return statistics;
    }

    // ===========================================
    // Вспомогательные методы
    // ===========================================

    /**
     * Формирует результирующую Map с данными инструментов
     */
    private void buildResultMap(Map<String, Object> result,
            List<ShareDTO> shares,
            List<FutureDTO> futures,
            List<IndicativeDTO> indicatives,
            List<ClosePriceDTO> closePrices,
            List<OpenPriceDTO> openPrices,
            List<ClosePriceEveningSessionDTO> closePriceEveningSessions,
            List<LastPriceDTO> lastPrices,
            List<DividendDto> dividends) {

        result.put("shares", shares);
        result.put("futures", futures);
        result.put("indicatives", indicatives);
        result.put("closePrices", closePrices);
        result.put("openPrices", openPrices);
        result.put("closePriceEveningSessions", closePriceEveningSessions);
        result.put("lastPrices", lastPrices);
        result.put("dividends", dividends);

        result.put("shares_size", shares.size());
        result.put("futures_size", futures.size());
        result.put("indicatives_size", indicatives.size());
        result.put("closePrices_size", closePrices.size());
        result.put("openPrices_size", openPrices.size());
        result.put("closePriceEveningSessions_size", closePriceEveningSessions.size());
        result.put("lastPrices_size", lastPrices.size());
        result.put("dividends_size", dividends.size());

        result.put("total_instruments",
                shares.size() + futures.size() + indicatives.size() + closePrices.size() + openPrices.size()
                        + closePriceEveningSessions.size() + lastPrices.size() + dividends.size());
    }

    /**
     * Формирует пустой результат при ошибке
     */
    private void buildEmptyResult(Map<String, Object> result) {
        Arrays.stream(InstrumentType.values()).forEach(type -> {
            String key = type.getCacheName().replace("Cache", "");
            result.put(key, new ArrayList<>());
            result.put(key + "_size", 0);
        });
        result.put("total_instruments", 0);
    }

    // ===========================================
    // Методы для получения данных о ценах по типу
    // ===========================================

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getPriceDataByType(String instrumentId, String priceType) {
        logger.debug("Получение данных о ценах для инструмента: {} типа: {}", instrumentId, priceType);

        try {
            // Получаем все данные об инструменте из кэша
            List<Object> allInstrumentData = getInstrumentByFigiFromCacheOnly(instrumentId);

            if (allInstrumentData == null || allInstrumentData.isEmpty()) {
                logger.warn("Данные об инструменте не найдены: {}", instrumentId);
                return new ArrayList<>();
            }

            // Фильтруем данные по типу цены
            List<Object> filteredData = allInstrumentData.stream()
                    .filter(data -> isDataOfPriceType(data, priceType))
                    .toList();

            logger.debug("Найдено {} записей типа {} для инструмента {}",
                    filteredData.size(), priceType, instrumentId);

            return filteredData;

        } catch (Exception e) {
            logger.error("Ошибка при получении данных о ценах для инструмента {} типа {}: {}",
                    instrumentId, priceType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDataOfPriceType(Object data, String priceType) {
        if (data == null || priceType == null) {
            return false;
        }

        String className = data.getClass().getSimpleName();

        return switch (priceType.toLowerCase()) {
            case "close_price" -> className.equals("ClosePriceDTO");
            case "close_price_evening_session" -> className.equals("ClosePriceEveningSessionDTO");
            case "open_price" -> className.equals("OpenPriceDTO");
            case "last_price" -> className.equals("LastPriceDTO");
            default -> {
                logger.warn("Неподдерживаемый тип цены: {}", priceType);
                yield false;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal extractPriceFromData(Object priceData) {
        if (priceData == null) {
            logger.warn("Объект с данными о цене равен null");
            return null;
        }

        try {
            String className = priceData.getClass().getSimpleName();

            return switch (className) {
                case "ClosePriceDTO" -> {
                    ClosePriceDTO closePriceDTO = (ClosePriceDTO) priceData;
                    yield closePriceDTO.closePrice();
                }
                case "ClosePriceEveningSessionDTO" -> {
                    ClosePriceEveningSessionDTO eveningSessionDTO = (ClosePriceEveningSessionDTO) priceData;
                    yield eveningSessionDTO.closePrice();
                }
                case "OpenPriceDTO" -> {
                    OpenPriceDTO openPriceDTO = (OpenPriceDTO) priceData;
                    yield openPriceDTO.openPrice();
                }
                case "LastPriceDTO" -> {
                    LastPriceDTO lastPriceDTO = (LastPriceDTO) priceData;
                    yield lastPriceDTO.price();
                }
                default -> {
                    logger.warn("Неподдерживаемый тип DTO для извлечения цены: {}", className);
                    yield null;
                }
            };

        } catch (ClassCastException e) {
            logger.error("Ошибка при приведении типа для извлечения цены: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при извлечении цены из объекта {}: {}",
                    priceData.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получает минимальный шаг цены для инструмента по FIGI из кэша.
     * 
     * <p>
     * Ищет инструмент среди акций и фьючерсов в кэше, возвращает minPriceIncrement.
     * Этот метод оптимизирован для быстрого доступа к данным из кэша.
     * </p>
     * 
     * @param figi идентификатор инструмента
     * @return минимальный шаг цены или null если не найден
     */
    @Override
    public BigDecimal getMinPriceIncrement(String figi) {
        logger.debug("Поиск минимального шага цены для инструмента: {}", figi);

        try {
            // Сначала ищем среди акций из кэша
            List<ShareDTO> shares = getSharesFromCacheOnly();
            for (ShareDTO share : shares) {
                if (figi.equals(share.figi()) && share.minPriceIncrement() != null) {
                    logger.debug("Найден шаг цены для акции {}: {}", figi, share.minPriceIncrement());
                    return share.minPriceIncrement();
                }
            }

            // Затем ищем среди фьючерсов из кэша
            List<FutureDTO> futures = getFuturesFromCacheOnly();
            for (FutureDTO future : futures) {
                if (figi.equals(future.figi()) && future.minPriceIncrement() != null) {
                    logger.debug("Найден шаг цены для фьючерса {}: {}", figi, future.minPriceIncrement());
                    return future.minPriceIncrement();
                }
            }

            logger.warn("Минимальный шаг цены не найден в кэше для инструмента: {}", figi);
            return null;

        } catch (Exception e) {
            logger.error("Ошибка при получении минимального шага цены для инструмента {}: {}",
                    figi, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получает размер лота для инструмента по FIGI из кэша.
     * 
     * @param figi идентификатор инструмента
     * @return размер лота или null если не найден
     */
    @Override
    public Integer getLot(String figi) {
        logger.debug("Поиск размера лота для инструмента: {}", figi);

        try {
            // Сначала ищем среди акций из кэша
            List<ShareDTO> shares = getSharesFromCacheOnly();
            for (ShareDTO share : shares) {
                if (figi.equals(share.figi()) && share.lot() != null) {
                    logger.debug("Найден размер лота для акции {}: {}", figi, share.lot());
                    return share.lot();
                }
            }

            // Затем ищем среди фьючерсов из кэша
            List<FutureDTO> futures = getFuturesFromCacheOnly();
            for (FutureDTO future : futures) {
                if (figi.equals(future.figi()) && future.lot() != null) {
                    logger.debug("Найден размер лота для фьючерса {}: {}", figi, future.lot());
                    return future.lot();
                }
            }

            logger.warn("Размер лота не найден в кэше для инструмента: {}", figi);
            return null;

        } catch (Exception e) {
            logger.error("Ошибка при получении размера лота для инструмента {}: {}",
                    figi, e.getMessage(), e);
            return null;
        }
    }
}
