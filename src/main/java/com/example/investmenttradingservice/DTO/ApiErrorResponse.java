package com.example.investmenttradingservice.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Стандартизированный DTO для ответов об ошибках API
 *
 * <p>
 * Предоставляет единообразный формат для всех ошибок API,
 * включая детальную информацию об ошибке, временные метки и контекст.
 * </p>
 *
 * <p>
 * Пример использования:
 * </p>
 * <pre>{@code
 * ApiErrorResponse error = ApiErrorResponse.builder()
 *     .message("Инструмент не найден")
 *     .errorCode("INSTRUMENT_NOT_FOUND")
 *     .status(404)
 *     .path("/api/instruments/INVALID_FIGI")
 *     .build();
 * }</pre>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /** Сообщение об ошибке */
    private String message;

    /** Код ошибки для программной обработки */
    private String errorCode;

    /** HTTP статус код */
    private int status;

    /** Путь запроса, который вызвал ошибку */
    private String path;

    /** Временная метка ошибки */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /** Дополнительные детали ошибки */
    private Map<String, Object> details;

    /** Список ошибок валидации (для 400 Bad Request) */
    private List<ValidationError> validationErrors;

    /** Трассировка стека (только в dev режиме) */
    private String trace;

    /** Дополнительные рекомендации по исправлению ошибки */
    private String suggestion;

    /** Контекст операции, в которой произошла ошибка */
    private String operationContext;

    /** Примеры корректных значений (для ошибок валидации) */
    private List<String> examples;

    /** Ссылка на документацию API */
    private String documentationUrl;

    /**
     * Конструктор по умолчанию
     */
    public ApiErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами
     *
     * @param message сообщение об ошибке
     * @param errorCode код ошибки
     * @param status HTTP статус
     */
    public ApiErrorResponse(String message, String errorCode, int status) {
        this();
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
    }

    /**
     * Создает builder для построения ответа об ошибке
     *
     * @return новый экземпляр ApiErrorResponseBuilder
     */
    public static ApiErrorResponseBuilder builder() {
        return new ApiErrorResponseBuilder();
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getOperationContext() {
        return operationContext;
    }

    public void setOperationContext(String operationContext) {
        this.operationContext = operationContext;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    /**
     * Builder класс для ApiErrorResponse
     */
    public static class ApiErrorResponseBuilder {
        private final ApiErrorResponse response;

        public ApiErrorResponseBuilder() {
            this.response = new ApiErrorResponse();
        }

        public ApiErrorResponseBuilder message(String message) {
            response.message = message;
            return this;
        }

        public ApiErrorResponseBuilder errorCode(String errorCode) {
            response.errorCode = errorCode;
            return this;
        }

        public ApiErrorResponseBuilder status(int status) {
            response.status = status;
            return this;
        }

        public ApiErrorResponseBuilder path(String path) {
            response.path = path;
            return this;
        }

        public ApiErrorResponseBuilder details(Map<String, Object> details) {
            response.details = details;
            return this;
        }

        public ApiErrorResponseBuilder validationErrors(List<ValidationError> validationErrors) {
            response.validationErrors = validationErrors;
            return this;
        }

        public ApiErrorResponseBuilder trace(String trace) {
            response.trace = trace;
            return this;
        }

        public ApiErrorResponseBuilder suggestion(String suggestion) {
            response.suggestion = suggestion;
            return this;
        }

        public ApiErrorResponseBuilder operationContext(String operationContext) {
            response.operationContext = operationContext;
            return this;
        }

        public ApiErrorResponseBuilder examples(List<String> examples) {
            response.examples = examples;
            return this;
        }

        public ApiErrorResponseBuilder documentationUrl(String documentationUrl) {
            response.documentationUrl = documentationUrl;
            return this;
        }

        public ApiErrorResponse build() {
            return response;
        }
    }

    /**
     * DTO для ошибок валидации
     */
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;

        public ValidationError() {}

        public ValidationError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        // Getters and Setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
