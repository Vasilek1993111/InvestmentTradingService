package com.example.investmenttradingservice.enums;

/**
 * Enum для типов инструментов в системе кэширования
 *
 * <p>
 * Определяет конфигурацию для каждого типа инструмента:
 * - имя кэша
 * - возможные ключи для поиска
 * - тип данных
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public enum InstrumentType {

    SHARES("sharesCache", new String[] { "all_shares", "|||", "|moex_mrng_evng_e_wknd_dlr|||" }),
    FUTURES("futuresCache", new String[] { "all_futures", "||||" }),
    INDICATIVES("indicativesCache", new String[] { "all_indicatives", "|||||" }),
    CLOSE_PRICES("closePricesCache", new String[] { "all_close_prices", "||||||" }),
    OPEN_PRICES("openPricesCache", new String[] { "all_open_prices", "|||||||" }),
    CLOSE_PRICES_EVENING_SESSION("closePricesEveningSessionCache",
            new String[] { "all_close_prices_evening_session", "||||||||" }),
    LAST_PRICES("lastPricesCache", new String[] { "all_last_prices", "|||||||||" }),
    DIVIDENDS("dividendsCache", new String[] { "all_dividends", "||||||||||" });

    /** Имя кэша для данного типа инструмента */
    private final String cacheName;

    /** Возможные ключи для поиска в кэше */
    private final String[] possibleKeys;

    /**
     * Конструктор для типа инструмента
     *
     * @param cacheName    имя кэша
     * @param possibleKeys возможные ключи для поиска
     */
    InstrumentType(String cacheName, String[] possibleKeys) {
        this.cacheName = cacheName;
        this.possibleKeys = possibleKeys;
    }

    /**
     * Получает имя кэша
     *
     * @return имя кэша
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Получает возможные ключи для поиска
     *
     * @return массив возможных ключей
     */
    public String[] getPossibleKeys() {
        return possibleKeys;
    }
}
