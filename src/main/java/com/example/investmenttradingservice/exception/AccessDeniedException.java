package com.example.investmenttradingservice.exception;

/**
 * Исключение для ошибок доступа к ресурсам
 *
 * <p>
 * Используется когда пользователь не имеет прав доступа к запрашиваемому ресурсу.
 * Возвращает HTTP 403 Forbidden.
 * </p>
 *
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * if (!hasPermission(user, resource)) {
 *     throw new AccessDeniedException("Недостаточно прав для доступа к ресурсу", resource);
 * }
 * }</pre>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class AccessDeniedException extends BaseApiException {

    /** Ресурс, к которому запрещен доступ */
    private final String resource;

    /**
     * Конструктор с сообщением и ресурсом
     *
     * @param message сообщение об ошибке
     * @param resource ресурс, к которому запрещен доступ
     */
    public AccessDeniedException(String message, String resource) {
        super(message, "ACCESS_DENIED", 403);
        this.resource = resource;
    }

    /**
     * Конструктор с сообщением, причиной и ресурсом
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param resource ресурс, к которому запрещен доступ
     */
    public AccessDeniedException(String message, Throwable cause, String resource) {
        super(message, cause, "ACCESS_DENIED", 403);
        this.resource = resource;
    }

    /**
     * Получить ресурс, к которому запрещен доступ
     *
     * @return ресурс
     */
    public String getResource() {
        return resource;
    }
}
