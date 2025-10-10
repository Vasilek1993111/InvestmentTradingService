package com.example.investmenttradingservice.exception;

/**
 * Исключение для ошибок бизнес-логики
 *
 * <p>
 * Используется когда операция не может быть выполнена из-за нарушений
 * бизнес-правил. Возвращает HTTP 422 Unprocessable Entity.
 * </p>
 *
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * if (order.getQuantity() <= 0) {
 *     throw new BusinessLogicException("Количество должно быть больше нуля", "INVALID_QUANTITY");
 * }
 * }</pre>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class BusinessLogicException extends BaseApiException {

    /**
     * Конструктор с сообщением и кодом ошибки
     *
     * @param message сообщение об ошибке
     * @param errorCode код ошибки
     */
    public BusinessLogicException(String message, String errorCode) {
        super(message, errorCode, 422);
    }

    /**
     * Конструктор с сообщением, причиной и кодом ошибки
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param errorCode код ошибки
     */
    public BusinessLogicException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode, 422);
    }
}
