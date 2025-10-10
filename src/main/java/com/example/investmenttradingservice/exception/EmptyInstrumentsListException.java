package com.example.investmenttradingservice.exception;

/**
 * Исключение для случаев, когда инструменты не найдены, но это нормальная ситуация
 *
 * <p>
 * Используется когда запрашивается список инструментов, но он пуст.
 * Возвращает HTTP 200 с пустым списком и соответствующим сообщением.
 * </p>
 *
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * if (instruments.isEmpty()) {
 *     throw new EmptyInstrumentsListException("Инструменты не найдены в кэше", "shares");
 * }
 * }</pre>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class EmptyInstrumentsListException extends BaseApiException {

    /** Тип инструментов, которые не найдены */
    private final String instrumentType;

    /**
     * Конструктор с сообщением и типом инструментов
     *
     * @param message сообщение об ошибке
     * @param instrumentType тип инструментов
     */
    public EmptyInstrumentsListException(String message, String instrumentType) {
        super(message, "EMPTY_INSTRUMENTS_LIST", 200); // Возвращаем 200, а не ошибку
        this.instrumentType = instrumentType;
    }

    /**
     * Конструктор с сообщением, причиной и типом инструментов
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param instrumentType тип инструментов
     */
    public EmptyInstrumentsListException(String message, Throwable cause, String instrumentType) {
        super(message, cause, "EMPTY_INSTRUMENTS_LIST", 200);
        this.instrumentType = instrumentType;
    }

    /**
     * Получить тип инструментов
     *
     * @return тип инструментов
     */
    public String getInstrumentType() {
        return instrumentType;
    }
}
