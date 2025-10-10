package com.example.investmenttradingservice.exception;

/**
 * Исключение для ошибок внешних API (например, Tinkoff Invest API)
 *
 * <p>
 * Используется когда внешний API возвращает ошибку или недоступен.
 * Возвращает HTTP 502 Bad Gateway.
 * </p>
 *
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * try {
 *     // вызов внешнего API
 * } catch (ExternalApiException e) {
 *     throw new ExternalApiException("Ошибка Tinkoff Invest API", e, "TINKOFF_API_ERROR");
 * }
 * }</pre>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class ExternalApiException extends BaseApiException {

    /** Название внешнего API */
    private final String apiName;

    /**
     * Конструктор с сообщением и названием API
     *
     * @param message сообщение об ошибке
     * @param apiName название внешнего API
     */
    public ExternalApiException(String message, String apiName) {
        super(message, "EXTERNAL_API_ERROR", 502);
        this.apiName = apiName;
    }

    /**
     * Конструктор с сообщением, причиной и названием API
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param apiName название внешнего API
     */
    public ExternalApiException(String message, Throwable cause, String apiName) {
        super(message, cause, "EXTERNAL_API_ERROR", 502);
        this.apiName = apiName;
    }

    /**
     * Конструктор с сообщением, причиной, кодом ошибки и названием API
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param errorCode код ошибки
     * @param apiName название внешнего API
     */
    public ExternalApiException(String message, Throwable cause, String errorCode, String apiName) {
        super(message, cause, errorCode, 502);
        this.apiName = apiName;
    }

    /**
     * Получить название внешнего API
     *
     * @return название API
     */
    public String getApiName() {
        return apiName;
    }
}
