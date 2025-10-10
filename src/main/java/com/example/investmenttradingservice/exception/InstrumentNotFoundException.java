package com.example.investmenttradingservice.exception;

/**
 * Исключение для случаев, когда инструмент не найден
 *
 * <p>
 * Используется когда запрашивается конкретный инструмент по FIGI,
 * но он не найден в системе. Возвращает HTTP 404.
 * </p>
 *
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * if (instrument == null) {
 *     throw new InstrumentNotFoundException("Инструмент с FIGI " + figi + " не найден", figi);
 * }
 * }</pre>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class InstrumentNotFoundException extends BaseApiException {

    /** FIGI инструмента, который не был найден */
    private final String figi;

    /**
     * Конструктор с сообщением и FIGI
     *
     * @param message сообщение об ошибке
     * @param figi FIGI инструмента
     */
    public InstrumentNotFoundException(String message, String figi) {
        super(message, "INSTRUMENT_NOT_FOUND", 404);
        this.figi = figi;
    }

    /**
     * Конструктор с сообщением, причиной и FIGI
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param figi FIGI инструмента
     */
    public InstrumentNotFoundException(String message, Throwable cause, String figi) {
        super(message, cause, "INSTRUMENT_NOT_FOUND", 404);
        this.figi = figi;
    }

    /**
     * Получить FIGI инструмента
     *
     * @return FIGI инструмента
     */
    public String getFigi() {
        return figi;
    }
}
