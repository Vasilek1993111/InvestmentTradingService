package com.example.investmenttradingservice.exception;

/**
 * Базовое исключение для всех ошибок API
 *
 * <p>
 * Предоставляет базовую функциональность для всех кастомных исключений,
 * включая коды ошибок и детальную информацию.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public abstract class BaseApiException extends RuntimeException {

    /** Код ошибки для программной обработки */
    private final String errorCode;

    /** HTTP статус код */
    private final int httpStatus;

    /**
     * Конструктор с сообщением
     *
     * @param message сообщение об ошибке
     * @param errorCode код ошибки
     * @param httpStatus HTTP статус код
     */
    public BaseApiException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * Конструктор с сообщением и причиной
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param errorCode код ошибки
     * @param httpStatus HTTP статус код
     */
    public BaseApiException(String message, Throwable cause, String errorCode, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * Получить код ошибки
     *
     * @return код ошибки
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Получить HTTP статус код
     *
     * @return HTTP статус код
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}
