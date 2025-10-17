package com.example.investmenttradingservice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.investmenttradingservice.DTO.*;

/**
 * Фасад для работы со всеми типами инструментов
 *
 * <p>
 * Предоставляет единую точку доступа ко всем сервисам инструментов.
 * Скрывает сложность взаимодействия с множественными сервисами
 * и предоставляет упрощенный API для клиентов.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех типов инструментов</li>
 * <li>Поиск инструментов по FIGI</li>
 * <li>Получение статистики по кэшам</li>
 * <li>Работа с кэшированными и некэшированными данными</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public interface InstrumentServiceFacade {

    // ===========================================
    // Методы для получения всех инструментов
    // ===========================================

    /**
     * Получает все инструменты из кэша с fallback на БД
     *
     * @return Map с инструментами и их размерами
     */
    Map<String, Object> getAllInstruments();

    /**
     * Получает все инструменты ТОЛЬКО из кэша (без fallback на БД)
     *
     * @return Map с инструментами и их размерами
     */
    Map<String, Object> getAllInstrumentsFromCacheOnly();

    // ===========================================
    // Методы для получения конкретных инструментов
    // ===========================================

    /**
     * Получает все акции
     *
     * @return список акций
     */
    List<ShareDTO> getShares();

    /**
     * Получает все фьючерсы
     *
     * @return список фьючерсов
     */
    List<FutureDTO> getFutures();

    /**
     * Получает все индикативы
     *
     * @return список индикативов
     */
    List<IndicativeDTO> getIndicatives();

    /**
     * Получает все цены закрытия
     *
     * @return список цен закрытия
     */
    List<ClosePriceDTO> getClosePrices();

    /**
     * Получает все цены открытия
     *
     * @return список цен открытия
     */
    List<OpenPriceDTO> getOpenPrices();

    /**
     * Получает все цены закрытия вечерней сессии
     *
     * @return список цен закрытия вечерней сессии
     */
    List<ClosePriceEveningSessionDTO> getClosePriceEveningSessions();

    /**
     * Получает все цены последних сделок
     *
     * @return список цен последних сделок
     */
    List<LastPriceDTO> getLastPrices();

    /**
     * Получает все дивиденды
     *
     * @return список дивидендов
     */
    List<DividendDto> getDividends();

    // ===========================================
    // Методы для получения инструментов только из кэша
    // ===========================================

    /**
     * Получает акции только из кэша
     *
     * @return список акций из кэша
     */
    List<ShareDTO> getSharesFromCacheOnly();

    /**
     * Получает фьючерсы только из кэша
     *
     * @return список фьючерсов из кэша
     */
    List<FutureDTO> getFuturesFromCacheOnly();

    /**
     * Получает индикативы только из кэша
     *
     * @return список индикативов из кэша
     */
    List<IndicativeDTO> getIndicativesFromCacheOnly();

    /**
     * Получает цены закрытия только из кэша
     *
     * @return список цен закрытия из кэша
     */
    List<ClosePriceDTO> getClosePricesFromCacheOnly();

    /**
     * Получает цены открытия только из кэша
     *
     * @return список цен открытия из кэша
     */
    List<OpenPriceDTO> getOpenPricesFromCacheOnly();

    /**
     * Получает цены закрытия вечерней сессии только из кэша
     *
     * @return список цен закрытия вечерней сессии из кэша
     */
    List<ClosePriceEveningSessionDTO> getClosePriceEveningSessionsFromCacheOnly();

    /**
     * Получает цены последних сделок только из кэша
     *
     * @return список цен последних сделок из кэша
     */
    List<LastPriceDTO> getLastPricesFromCacheOnly();

    /**
     * Получает дивиденды только из кэша
     *
     * @return список дивидендов из кэша
     */
    List<DividendDto> getDividendsFromCacheOnly();

    // ===========================================
    // Методы для поиска по FIGI
    // ===========================================

    /**
     * Получает информацию об инструменте по FIGI из всех источников
     *
     * @param figi идентификатор инструмента
     * @return список всех найденных записей
     */
    List<Object> getInstrumentByFigi(String figi);

    /**
     * Получает информацию об инструменте по FIGI только из кэша
     *
     * @param figi идентификатор инструмента
     * @return список всех найденных записей из кэша
     */
    List<Object> getInstrumentByFigiFromCacheOnly(String figi);

    // ===========================================
    // Методы для получения статистики
    // ===========================================

    /**
     * Получает статистику по всем кэшам
     *
     * @return Map со статистикой кэшей
     */
    Map<String, Object> getCacheStatistics();

    // ===========================================
    // Методы для получения данных о ценах по типу
    // ===========================================

    /**
     * Получает данные о ценах для инструмента по типу цены.
     * 
     * @param instrumentId идентификатор инструмента
     * @param priceType    тип цены (close_price, open_price, last_price,
     *                     close_price_evening_session)
     * @return список данных о ценах соответствующего типа
     */
    List<Object> getPriceDataByType(String instrumentId, String priceType);

    /**
     * Проверяет, соответствует ли объект данных указанному типу цены.
     * 
     * @param data      объект данных
     * @param priceType тип цены
     * @return true если данные соответствуют типу цены
     */
    boolean isDataOfPriceType(Object data, String priceType);

    /**
     * Извлекает цену из объекта данных в зависимости от типа DTO.
     * 
     * @param priceData объект с данными о цене
     * @return цена или null если не удалось извлечь
     */
    BigDecimal extractPriceFromData(Object priceData);

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
    BigDecimal getMinPriceIncrement(String figi);

    /**
     * Получает размер лота для инструмента по FIGI из кэша.
     * 
     * @param figi идентификатор инструмента
     * @return размер лота или null если не найден
     */
    Integer getLot(String figi);
}
