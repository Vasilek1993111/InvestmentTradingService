package com.example.investmenttradingservice.exception;

/**
 * Исключение для ошибок валидации данных
 *
 * <p>
 * Используется когда входящие данные не проходят валидацию.
 * Возвращает HTTP 400 Bad Request.
 * </p>
 *
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * if (figi == null || figi.trim().isEmpty()) {
 *     throw new ValidationException("FIGI не может быть пустым", "figi", figi);
 * }
 * }</pre>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class ValidationException extends BaseApiException {

    /** Поле, которое не прошло валидацию */
    private final String field;

    /** Значение, которое не прошло валидацию */
    private final Object rejectedValue;

    /**
     * Конструктор с сообщением и полем
     *
     * @param message сообщение об ошибке
     * @param field поле, которое не прошло валидацию
     */
    public ValidationException(String message, String field) {
        super(message, "VALIDATION_ERROR", 400);
        this.field = field;
        this.rejectedValue = null;
    }

    /**
     * Конструктор с сообщением, полем и отклоненным значением
     *
     * @param message сообщение об ошибке
     * @param field поле, которое не прошло валидацию
     * @param rejectedValue значение, которое не прошло валидацию
     */
    public ValidationException(String message, String field, Object rejectedValue) {
        super(message, "VALIDATION_ERROR", 400);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Конструктор с сообщением, причиной и полем
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param field поле, которое не прошло валидацию
     */
    public ValidationException(String message, Throwable cause, String field) {
        super(message, cause, "VALIDATION_ERROR", 400);
        this.field = field;
        this.rejectedValue = null;
    }

    /**
     * Получить поле, которое не прошло валидацию
     *
     * @return поле
     */
    public String getField() {
        return field;
    }

    /**
     * Получить значение, которое не прошло валидацию
     *
     * @return отклоненное значение
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }
}
