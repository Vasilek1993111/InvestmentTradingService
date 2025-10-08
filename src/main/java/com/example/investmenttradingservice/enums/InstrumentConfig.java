package com.example.investmenttradingservice.enums;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Enum для конфигурации инструментов в системе кэширования
 *
 * <p>
 * Определяет полную конфигурацию для каждого типа инструмента:
 * - тип инструмента (кэш и ключи)
 * - supplier для загрузки данных из БД
 * - функция для преобразования Entity в DTO
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public enum InstrumentConfig {

    SHARES(InstrumentType.SHARES, null, null),
    FUTURES(InstrumentType.FUTURES, null, null),
    INDICATIVES(InstrumentType.INDICATIVES, null, null),
    CLOSE_PRICES(InstrumentType.CLOSE_PRICES, null, null),
    OPEN_PRICES(InstrumentType.OPEN_PRICES, null, null), 
    CLOSE_PRICES_EVENING_SESSION(InstrumentType.CLOSE_PRICES_EVENING_SESSION, null, null);

    /** Тип инструмента */
    private final InstrumentType instrumentType;

    /** Supplier для загрузки данных из БД (будет установлен через setter) */
    private Supplier<?> dbSupplier;

    /** Функция для преобразования Entity в DTO (будет установлена через setter) */
    private Function<?, ?> mapperFunction;

    /**
     * Конструктор для конфигурации инструмента
     *
     * @param instrumentType тип инструмента
     * @param dbSupplier     supplier для загрузки данных из БД
     * @param mapperFunction функция для преобразования Entity в DTO
     */
    InstrumentConfig(InstrumentType instrumentType, Supplier<?> dbSupplier, Function<?, ?> mapperFunction) {
        this.instrumentType = instrumentType;
        this.dbSupplier = dbSupplier;
        this.mapperFunction = mapperFunction;
    }

    /**
     * Получает тип инструмента
     *
     * @return тип инструмента
     */
    public InstrumentType getInstrumentType() {
        return instrumentType;
    }

    /**
     * Получает supplier для загрузки данных из БД
     *
     * @return supplier
     */
    public Supplier<?> getDbSupplier() {
        return dbSupplier;
    }

    /**
     * Получает функцию для преобразования Entity в DTO
     *
     * @return функция преобразования
     */
    public Function<?, ?> getMapperFunction() {
        return mapperFunction;
    }

    /**
     * Устанавливает supplier для загрузки данных из БД
     *
     * @param dbSupplier supplier
     */
    public void setDbSupplier(Supplier<?> dbSupplier) {
        this.dbSupplier = dbSupplier;
    }

    /**
     * Устанавливает функцию для преобразования Entity в DTO
     *
     * @param mapperFunction функция преобразования
     */
    public void setMapperFunction(Function<?, ?> mapperFunction) {
        this.mapperFunction = mapperFunction;
    }
}
