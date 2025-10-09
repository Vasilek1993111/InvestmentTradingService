package com.example.investmenttradingservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.example.investmenttradingservice.entity.LastPriceEntity;
import com.example.investmenttradingservice.entity.DividendEntity;
import com.example.investmenttradingservice.repository.ClosePriceEveningSessionRepository;
import com.example.investmenttradingservice.repository.ClosePriceRepository;
import com.example.investmenttradingservice.repository.FutureRepository;
import com.example.investmenttradingservice.repository.Indicativerepository;
import com.example.investmenttradingservice.repository.OpenPriceRepositrory;
import com.example.investmenttradingservice.repository.ShareRepository;
import com.example.investmenttradingservice.repository.LastPriceRepository;
import com.example.investmenttradingservice.repository.DivedendsRepository;
import com.example.investmenttradingservice.util.WorkingDaysUtils;
import com.example.investmenttradingservice.DTO.ClosePriceDTO;
import com.example.investmenttradingservice.DTO.ClosePriceEveningSessionDTO;
import com.example.investmenttradingservice.DTO.LastPriceDTO;
import com.example.investmenttradingservice.DTO.DividendDto;
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

    /** Репозиторий для работы с ценами последних сделок */
    private final LastPriceRepository lastPriceRepository;

    /** Репозиторий для работы с дивидендами */
    private final DivedendsRepository divedendsRepository;

    /** Менеджер кэша для управления кэшированием */
    private final CacheManager cacheManager;

    /** Маппер для преобразования Entity в DTO и обратно */
    @Autowired
    private Mapper mapper;

    /**
     * Конструктор сервиса кэшированных инструментов
     *
     * @param shareRepository                    репозиторий для работы с акциями
     * @param futureRepository                   репозиторий для работы с фьючерсами
     * @param indicativeRepository               репозиторий для работы с
     *                                           индикативами
     * @param closePriceRepository               репозиторий для работы с ценами
     *                                           закрытия
     * @param openPriceRepository                репозиторий для работы с ценами
     *                                           открытия
     * @param closePriceEveningSessionRepository репозиторий для работы с ценами
     *                                           закрытия вечерней сессии
     * @param lastPriceRepository                репозиторий для работы с ценами
     *                                           последних сделок
     * @param divedendsRepository                репозиторий для работы с
     *                                           дивидендами
     * @param cacheManager                       менеджер кэша
     */
    public CacheInstrumentsService(ShareRepository shareRepository, FutureRepository futureRepository,
            Indicativerepository indicativeRepository, ClosePriceRepository closePriceRepository,
            OpenPriceRepositrory openPriceRepository,
            ClosePriceEveningSessionRepository closePriceEveningSessionRepository,
            LastPriceRepository lastPriceRepository,
            DivedendsRepository divedendsRepository,
            CacheManager cacheManager) {
        this.shareRepository = shareRepository;
        this.futureRepository = futureRepository;
        this.indicativeRepository = indicativeRepository;
        this.closePriceRepository = closePriceRepository;
        this.openPriceRepository = openPriceRepository;
        this.closePriceEveningSessionRepository = closePriceEveningSessionRepository;
        this.lastPriceRepository = lastPriceRepository;
        this.divedendsRepository = divedendsRepository;
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
            List<LastPriceDTO> lastPrices = getInstrumentData(InstrumentType.LAST_PRICES, true);
            List<DividendDto> dividends = getInstrumentData(InstrumentType.DIVIDENDS, true);

            // Формируем результат
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

            logger.info(
                    "Получены инструменты только из кэша: {} акций, {} фьючерсов, {} индикативов, {} цен закрытия, {} цен открытия, {} цен последних сделок, {} дивидендов",
                    shares.size(), futures.size(), indicatives.size(), closePrices.size(), openPrices.size(),
                    lastPrices.size(), dividends.size());

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

    /**
     * Получает список всех цен последних сделок из кэша с fallback на базу данных
     *
     * <p>
     * Сначала пытается получить цены последних сделок из кэша lastPricesCache. Если
     * кэш
     * пуст или произошла ошибка, загружает данные из базы данных через
     * LastPriceRepository.
     * </p>
     *
     * @return список цен последних сделок в формате LastPriceDTO
     */
    @Transactional
    public List<LastPriceDTO> getLastPrices() {
        return getInstrumentData(InstrumentType.LAST_PRICES, false);
    }

    /**
     * Получает список всех цен последних сделок ТОЛЬКО из кэша (без fallback на БД)
     *
     * @return список цен последних сделок в формате LastPriceDTO (может быть
     *         пустым)
     */
    public List<LastPriceDTO> getLastPricesFromCacheOnly() {
        return getInstrumentData(InstrumentType.LAST_PRICES, true);
    }

    /**
     * Получает список всех дивидендов из кэша с fallback на базу данных
     *
     * <p>
     * Сначала пытается получить дивиденды из кэша dividendsCache. Если кэш
     * пуст или произошла ошибка, загружает данные из базы данных через
     * DivedendsRepository.
     * </p>
     *
     * @return список дивидендов в формате DividendDto
     */
    @Transactional
    public List<DividendDto> getDividends() {
        return getInstrumentData(InstrumentType.DIVIDENDS, false);
    }

    /**
     * Получает список всех дивидендов ТОЛЬКО из кэша (без fallback на БД)
     *
     * @return список дивидендов в формате DividendDto (может быть пустым)
     */
    public List<DividendDto> getDividendsFromCacheOnly() {
        return getInstrumentData(InstrumentType.DIVIDENDS, true);
    }

    /**
     * Получает информацию об инструменте по FIGI из всех доступных кэшей
     *
     * <p>
     * Этот метод выполняет поиск инструмента по FIGI во всех типах кэшей:
     * акции, фьючерсы, индикативы, цены закрытия, цены открытия, цены последних
     * сделок и дивиденды.
     * Возвращает список всех найденных записей, связанных с указанным FIGI.
     * </p>
     *
     * <p>
     * Процесс поиска:
     * </p>
     * <ul>
     * <li>Поиск в кэше акций по FIGI</li>
     * <li>Поиск в кэше фьючерсов по FIGI</li>
     * <li>Поиск в кэше индикативов по FIGI</li>
     * <li>Поиск в кэше цен закрытия по FIGI</li>
     * <li>Поиск в кэше цен открытия по FIGI</li>
     * <li>Поиск в кэше цен последних сделок по FIGI</li>
     * <li>Поиск в кэше дивидендов по FIGI</li>
     * <li>Объединение всех найденных результатов</li>
     * </ul>
     *
     * @param figi идентификатор инструмента для поиска
     * @return список всех найденных записей, связанных с FIGI (может быть пустым)
     */
    public List<Object> getInstrumentByFigi(String figi) {
        if (figi == null || figi.trim().isEmpty()) {
            logger.warn("FIGI не может быть пустым");
            return new ArrayList<>();
        }

        List<Object> allResults = new ArrayList<>();
        String trimmedFigi = figi.trim();

        logger.info("Поиск инструмента по FIGI: {}", trimmedFigi);

        try {
            // Поиск в акциях
            List<ShareDTO> shares = findSharesByFigi(trimmedFigi);
            allResults.addAll(shares);

            // Поиск в фьючерсах
            List<FutureDTO> futures = findFuturesByFigi(trimmedFigi);
            allResults.addAll(futures);

            // Поиск в индикативах
            List<IndicativeDTO> indicatives = findIndicativesByFigi(trimmedFigi);
            allResults.addAll(indicatives);

            // Поиск в ценах закрытия
            List<ClosePriceDTO> closePrices = findClosePricesByFigi(trimmedFigi);
            allResults.addAll(closePrices);

            // Поиск в ценах открытия
            List<OpenPriceDTO> openPrices = findOpenPricesByFigi(trimmedFigi);
            allResults.addAll(openPrices);

            // Поиск в ценах последних сделок
            List<LastPriceDTO> lastPrices = findLastPricesByFigi(trimmedFigi);
            allResults.addAll(lastPrices);

            // Поиск в дивидендах
            List<DividendDto> dividends = findDividendsByFigi(trimmedFigi);
            allResults.addAll(dividends);

            logger.info("Найдено {} записей для FIGI: {}", allResults.size(), trimmedFigi);

        } catch (Exception e) {
            logger.error("Ошибка при поиске инструмента по FIGI {}: {}", trimmedFigi, e.getMessage(), e);
        }

        return allResults;
    }

    /**
     * Получает информацию об инструменте по FIGI ТОЛЬКО из кэша (без fallback на
     * БД)
     *
     * <p>
     * Этот метод выполняет поиск инструмента по FIGI только в кэшированных данных.
     * Не обращается к базе данных, поэтому работает быстрее, но может не найти
     * инструмент, если он не закэширован.
     * </p>
     *
     * @param figi идентификатор инструмента для поиска
     * @return список всех найденных записей из кэша, связанных с FIGI (может быть
     *         пустым)
     */
    public List<Object> getInstrumentByFigiFromCacheOnly(String figi) {
        if (figi == null || figi.trim().isEmpty()) {
            logger.warn("FIGI не может быть пустым");
            return new ArrayList<>();
        }

        List<Object> allResults = new ArrayList<>();
        String trimmedFigi = figi.trim();

        logger.info("Поиск инструмента по FIGI только в кэше: {}", trimmedFigi);

        try {
            // Поиск в акциях (только кэш)
            List<ShareDTO> shares = findSharesByFigiFromCacheOnly(trimmedFigi);
            allResults.addAll(shares);

            // Поиск в фьючерсах (только кэш)
            List<FutureDTO> futures = findFuturesByFigiFromCacheOnly(trimmedFigi);
            allResults.addAll(futures);

            // Поиск в индикативах (только кэш)
            List<IndicativeDTO> indicatives = findIndicativesByFigiFromCacheOnly(trimmedFigi);
            allResults.addAll(indicatives);

            // Поиск в ценах закрытия (только кэш)
            List<ClosePriceDTO> closePrices = findClosePricesByFigiFromCacheOnly(trimmedFigi);
            allResults.addAll(closePrices);

            // Поиск в ценах открытия (только кэш)
            List<OpenPriceDTO> openPrices = findOpenPricesByFigiFromCacheOnly(trimmedFigi);
            allResults.addAll(openPrices);

            // Поиск в ценах последних сделок (только кэш)
            List<LastPriceDTO> lastPrices = findLastPricesByFigiFromCacheOnly(trimmedFigi);
            allResults.addAll(lastPrices);

            // Поиск в дивидендах (только кэш)
            List<DividendDto> dividends = findDividendsByFigiFromCacheOnly(trimmedFigi);
            allResults.addAll(dividends);

            logger.info("Найдено {} записей в кэше для FIGI: {}", allResults.size(), trimmedFigi);

        } catch (Exception e) {
            logger.error("Ошибка при поиске инструмента по FIGI в кэше {}: {}", trimmedFigi, e.getMessage(), e);
        }

        return allResults;
    }

    // ===========================================
    // Вспомогательные методы для поиска по FIGI
    // ===========================================

    /**
     * Поиск акций по FIGI в кэше с fallback на БД
     */
    private List<ShareDTO> findSharesByFigi(String figi) {
        List<ShareDTO> shares = getShares();
        return shares.stream()
                .filter(share -> figi.equals(share.figi()))
                .toList();
    }

    /**
     * Поиск акций по FIGI только в кэше
     */
    private List<ShareDTO> findSharesByFigiFromCacheOnly(String figi) {
        List<ShareDTO> shares = getSharesFromCacheOnly();
        return shares.stream()
                .filter(share -> figi.equals(share.figi()))
                .toList();
    }

    /**
     * Поиск фьючерсов по FIGI в кэше с fallback на БД
     */
    private List<FutureDTO> findFuturesByFigi(String figi) {
        List<FutureDTO> futures = getFutures();
        return futures.stream()
                .filter(future -> figi.equals(future.figi()))
                .toList();
    }

    /**
     * Поиск фьючерсов по FIGI только в кэше
     */
    private List<FutureDTO> findFuturesByFigiFromCacheOnly(String figi) {
        List<FutureDTO> futures = getFuturesFromCacheOnly();
        return futures.stream()
                .filter(future -> figi.equals(future.figi()))
                .toList();
    }

    /**
     * Поиск индикативов по FIGI в кэше с fallback на БД
     */
    private List<IndicativeDTO> findIndicativesByFigi(String figi) {
        List<IndicativeDTO> indicatives = getIndicatives();
        return indicatives.stream()
                .filter(indicative -> figi.equals(indicative.figi()))
                .toList();
    }

    /**
     * Поиск индикативов по FIGI только в кэше
     */
    private List<IndicativeDTO> findIndicativesByFigiFromCacheOnly(String figi) {
        List<IndicativeDTO> indicatives = getIndicativesFromCacheOnly();
        return indicatives.stream()
                .filter(indicative -> figi.equals(indicative.figi()))
                .toList();
    }

    /**
     * Поиск цен закрытия по FIGI в кэше с fallback на БД
     */
    private List<ClosePriceDTO> findClosePricesByFigi(String figi) {
        List<ClosePriceDTO> closePrices = getClosePrices();
        return closePrices.stream()
                .filter(price -> figi.equals(price.figi()))
                .toList();
    }

    /**
     * Поиск цен закрытия по FIGI только в кэше
     */
    private List<ClosePriceDTO> findClosePricesByFigiFromCacheOnly(String figi) {
        List<ClosePriceDTO> closePrices = getClosePricesFromCacheOnly();
        return closePrices.stream()
                .filter(price -> figi.equals(price.figi()))
                .toList();
    }

    /**
     * Поиск цен открытия по FIGI в кэше с fallback на БД
     */
    private List<OpenPriceDTO> findOpenPricesByFigi(String figi) {
        List<OpenPriceDTO> openPrices = getOpenPrices();
        return openPrices.stream()
                .filter(price -> figi.equals(price.figi()))
                .toList();
    }

    /**
     * Поиск цен открытия по FIGI только в кэше
     */
    private List<OpenPriceDTO> findOpenPricesByFigiFromCacheOnly(String figi) {
        List<OpenPriceDTO> openPrices = getOpenPricesFromCacheOnly();
        return openPrices.stream()
                .filter(price -> figi.equals(price.figi()))
                .toList();
    }

    /**
     * Поиск цен последних сделок по FIGI в кэше с fallback на БД
     */
    private List<LastPriceDTO> findLastPricesByFigi(String figi) {
        List<LastPriceDTO> lastPrices = getLastPrices();
        return lastPrices.stream()
                .filter(price -> figi.equals(price.figi()))
                .toList();
    }

    /**
     * Поиск цен последних сделок по FIGI только в кэше
     */
    private List<LastPriceDTO> findLastPricesByFigiFromCacheOnly(String figi) {
        List<LastPriceDTO> lastPrices = getLastPricesFromCacheOnly();
        return lastPrices.stream()
                .filter(price -> figi.equals(price.figi()))
                .toList();
    }

    /**
     * Поиск дивидендов по FIGI в кэше с fallback на БД
     */
    private List<DividendDto> findDividendsByFigi(String figi) {
        List<DividendDto> dividends = getDividends();
        return dividends.stream()
                .filter(dividend -> figi.equals(dividend.figi()))
                .toList();
    }

    /**
     * Поиск дивидендов по FIGI только в кэше
     */
    private List<DividendDto> findDividendsByFigiFromCacheOnly(String figi) {
        List<DividendDto> dividends = getDividendsFromCacheOnly();
        return dividends.stream()
                .filter(dividend -> figi.equals(dividend.figi()))
                .toList();
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

        InstrumentConfig.LAST_PRICES.setDbSupplier(() -> lastPriceRepository.findAllLatestPrices());
        InstrumentConfig.LAST_PRICES
                .setMapperFunction((List<LastPriceEntity> entities) -> mapper.toLastPriceDTOList(entities));

        InstrumentConfig.DIVIDENDS.setDbSupplier(() -> divedendsRepository.findDividendsForTodayAndTomorrow());
        InstrumentConfig.DIVIDENDS
                .setMapperFunction((List<DividendEntity> entities) -> mapper.toDividendDtoList(entities));
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
     * Получает дату запроса для указанного типа инструмента
     *
     * <p>
     * Определяет дату, за которую были запрошены данные из БД.
     * Для цен закрытия, открытия и вечерней сессии используется предыдущий рабочий
     * день.
     * Для последних цен и дивидендов используется текущая дата.
     * </p>
     *
     * @param instrumentType тип инструмента
     * @return дата запроса данных
     */
    public LocalDateTime getRequestDateForInstrument(InstrumentType instrumentType) {
        return switch (instrumentType) {
            case CLOSE_PRICES, OPEN_PRICES, CLOSE_PRICES_EVENING_SESSION ->
                WorkingDaysUtils.getPreviousWorkingDay(LocalDate.now()).atStartOfDay();
            case LAST_PRICES, DIVIDENDS ->
                LocalDate.now().atStartOfDay();
            default ->
                LocalDate.now().atStartOfDay();
        };
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
