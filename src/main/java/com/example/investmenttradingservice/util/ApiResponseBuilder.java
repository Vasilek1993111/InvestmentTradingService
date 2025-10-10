package com.example.investmenttradingservice.util;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;

/**
 * Утилитный класс для создания стандартизированных API ответов
 *
 * <p>
 * Предоставляет методы для создания успешных ответов и ответов с ошибками
 * в едином формате для всех endpoints контроллера.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class ApiResponseBuilder {

    private final Map<String, Object> response;
    private final boolean isSuccess;

    private ApiResponseBuilder(boolean isSuccess) {
        this.response = new LinkedHashMap<>();
        this.isSuccess = isSuccess;
        this.response.put("success", isSuccess);
        this.response.put("status", isSuccess ? "success" : "error");
        this.response.put("timestamp", LocalDateTime.now());
    }

    /**
     * Создает builder для успешного ответа
     */
    public static ApiResponseBuilder success() {
        return new ApiResponseBuilder(true);
    }

    /**
     * Создает builder для ответа с ошибкой
     */
    public static ApiResponseBuilder error() {
        return new ApiResponseBuilder(false);
    }

    /**
     * Добавляет поле в ответ
     */
    public ApiResponseBuilder add(String key, Object value) {
        this.response.put(key, value);
        return this;
    }

    /**
     * Строит финальный Map ответа
     */
    public Map<String, Object> build() {
        return this.response;
    }

    /**
     * Создает успешный ответ с данными
     *
     * @param message сообщение об успехе
     * @param data    данные для возврата
     * @param size    размер данных
     * @param dataKey ключ для данных в ответе
     * @return ResponseEntity с успешным ответом
     */
    public static ResponseEntity<Object> success(String message, Object data, int size, String dataKey) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", "success");
        response.put("message", message);
        response.put(dataKey + "_size", size);
        response.put(dataKey, data);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok().body(response);
    }

    /**
     * Создает успешный ответ с данными и датой запроса
     *
     * @param message     сообщение об успехе
     * @param data        данные для возврата
     * @param size        размер данных
     * @param dataKey     ключ для данных в ответе
     * @param requestDate дата запроса данных из БД
     * @return ResponseEntity с успешным ответом
     */
    public static ResponseEntity<Object> success(String message, Object data, int size, String dataKey,
            LocalDateTime requestDate) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", "success");
        response.put("message", message);
        response.put(dataKey + "_size", size);
        response.put(dataKey, data);
        response.put("request_date", requestDate);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok().body(response);
    }

    /**
     * Создает успешный ответ с данными (использует "data" как ключ по умолчанию)
     *
     * @param message сообщение об успехе
     * @param data    данные для возврата
     * @param size    размер данных
     * @return ResponseEntity с успешным ответом
     */
    public static ResponseEntity<Object> success(String message, Object data, int size) {
        return success(message, data, size, "data");
    }

    /**
     * Создает ответ с ошибкой
     *
     * @param message сообщение об ошибке
     * @param e       исключение
     * @return ResponseEntity с ошибкой
     */
    public static ResponseEntity<Object> error(String message, Exception e) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", "error");
        errorResponse.put("message", message + ": " + e.getMessage());
        errorResponse.put("error", e.getClass().getSimpleName());
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.internalServerError().body(errorResponse);
    }

    /**
     * Создает ответ с ошибкой без исключения
     *
     * @param message сообщение об ошибке
     * @return ResponseEntity с ошибкой
     */
    public static ResponseEntity<Object> error(String message) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.internalServerError().body(errorResponse);
    }

    /**
     * Создает успешный ответ с данными и указанным HTTP статусом
     *
     * @param message    сообщение об успехе
     * @param data       данные для возврата
     * @param httpStatus HTTP статус ответа
     * @return ResponseEntity с успешным ответом
     */
    public static ResponseEntity<Map<String, Object>> success(String message, Object data,
            org.springframework.http.HttpStatus httpStatus) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Создает ответ с ошибкой и указанным HTTP статусом
     *
     * @param message    сообщение об ошибке
     * @param httpStatus HTTP статус ответа
     * @return ResponseEntity с ошибкой
     */
    public static ResponseEntity<Map<String, Object>> error(String message,
            org.springframework.http.HttpStatus httpStatus) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
