package com.example.investmenttradingservice.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Стандартизированный DTO для успешных ответов API
 *
 * <p>
 * Предоставляет единообразный формат для всех успешных ответов API,
 * включая данные, метаинформацию и временные метки.
 * </p>
 *
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * ApiSuccessResponse<List<ShareDTO>> response = ApiSuccessResponse.<List<ShareDTO>>builder()
 *     .message("Акции успешно получены")
 *     .data(shares)
 *     .totalCount(shares.size())
 *     .dataType("shares")
 *     .build();
 * }</pre>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiSuccessResponse<T> {

    /** Сообщение об успешном выполнении */
    private String message;

    /** Данные ответа */
    private T data;

    /** Общее количество элементов */
    private Integer totalCount;

    /** Тип данных */
    private String dataType;

    /** HTTP статус код */
    private int status;

    /** Временная метка ответа */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /** Дополнительные метаданные */
    private Map<String, Object> metadata;

    /** Индикатор успешности операции */
    private boolean success = true;

    /**
     * Конструктор по умолчанию
     */
    public ApiSuccessResponse() {
        this.timestamp = LocalDateTime.now();
        this.status = 200;
    }

    /**
     * Конструктор с основными параметрами
     *
     * @param message сообщение об успехе
     * @param data данные ответа
     * @param totalCount общее количество элементов
     */
    public ApiSuccessResponse(String message, T data, Integer totalCount) {
        this();
        this.message = message;
        this.data = data;
        this.totalCount = totalCount;
    }

    /**
     * Создает builder для построения успешного ответа
     *
     * @param <T> тип данных
     * @return новый экземпляр ApiSuccessResponseBuilder
     */
    public static <T> ApiSuccessResponseBuilder<T> builder() {
        return new ApiSuccessResponseBuilder<>();
    }

    /**
     * Создает успешный ответ для пустого списка инструментов
     *
     * @param message сообщение
     * @param dataType тип данных
     * @return успешный ответ с пустым списком
     */
    public static <T> ApiSuccessResponse<List<T>> emptyList(String message, String dataType) {
        return ApiSuccessResponse.<List<T>>builder()
                .message(message)
                .data(List.of())
                .totalCount(0)
                .dataType(dataType)
                .build();
    }

    /**
     * Создает успешный ответ для одного элемента
     *
     * @param message сообщение
     * @param data данные
     * @param dataType тип данных
     * @return успешный ответ с одним элементом
     */
    public static <T> ApiSuccessResponse<T> single(String message, T data, String dataType) {
        return ApiSuccessResponse.<T>builder()
                .message(message)
                .data(data)
                .totalCount(1)
                .dataType(dataType)
                .build();
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Builder класс для ApiSuccessResponse
     */
    public static class ApiSuccessResponseBuilder<T> {
        private final ApiSuccessResponse<T> response;

        public ApiSuccessResponseBuilder() {
            this.response = new ApiSuccessResponse<>();
        }

        public ApiSuccessResponseBuilder<T> message(String message) {
            response.message = message;
            return this;
        }

        public ApiSuccessResponseBuilder<T> data(T data) {
            response.data = data;
            return this;
        }

        public ApiSuccessResponseBuilder<T> totalCount(Integer totalCount) {
            response.totalCount = totalCount;
            return this;
        }

        public ApiSuccessResponseBuilder<T> dataType(String dataType) {
            response.dataType = dataType;
            return this;
        }

        public ApiSuccessResponseBuilder<T> status(int status) {
            response.status = status;
            return this;
        }

        public ApiSuccessResponseBuilder<T> metadata(Map<String, Object> metadata) {
            response.metadata = metadata;
            return this;
        }

        public ApiSuccessResponseBuilder<T> addMetadata(String key, Object value) {
            if (response.metadata == null) {
                response.metadata = new java.util.HashMap<>();
            }
            response.metadata.put(key, value);
            return this;
        }

        public ApiSuccessResponse<T> build() {
            return response;
        }
    }
}
