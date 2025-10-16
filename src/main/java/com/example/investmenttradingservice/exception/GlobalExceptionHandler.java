package com.example.investmenttradingservice.exception;

import com.example.investmenttradingservice.DTO.ApiErrorResponse;
import com.example.investmenttradingservice.util.ErrorContextBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Глобальный обработчик исключений для всех контроллеров
 *
 * <p>
 * Обрабатывает все исключения, возникающие в контроллерах, и возвращает
 * стандартизированные JSON-ответы с соответствующими HTTP статус кодами.
 * </p>
 *
 * <p>
 * Особенности обработки:
 * </p>
 * <ul>
 * <li>Кастомные исключения с детальной информацией</li>
 * <li>Стандартные Spring исключения</li>
 * <li>Валидация входных данных</li>
 * <li>Логирование ошибок</li>
 * <li>Скрытие внутренних деталей в production</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Режим разработки для отображения детальной информации об ошибках */
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    /**
     * Обработка кастомных исключений API
     *
     * @param ex      исключение
     * @param request HTTP запрос
     * @return стандартизированный ответ об ошибке
     */
    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseApiException(BaseApiException ex, WebRequest request) {
        logger.warn("API Exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        String requestPath = getRequestPath(request);
        String operationContext = getOperationContext(requestPath);

        ApiErrorResponse errorResponse;

        // Создаем детальные ошибки в зависимости от типа исключения
        if (ex instanceof InstrumentNotFoundException) {
            InstrumentNotFoundException instrumentEx = (InstrumentNotFoundException) ex;
            errorResponse = ErrorContextBuilder.buildInstrumentNotFoundError(
                    instrumentEx.getFigi(), operationContext);
        } else if (ex instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) ex;
            String expected = getExpectedFormat(validationEx.getField());
            // Специальный случай для start_time в прошлом
            if ("start_time".equalsIgnoreCase(validationEx.getField())) {
                expected = "Время в формате HH:mm:ss и не в прошлом (Europe/Moscow)";
            }
            errorResponse = ErrorContextBuilder.buildValidationError(
                    validationEx.getField(),
                    validationEx.getRejectedValue(),
                    expected,
                    operationContext);
        } else if (ex instanceof ExternalApiException) {
            ExternalApiException externalEx = (ExternalApiException) ex;
            errorResponse = ErrorContextBuilder.buildExternalApiError(
                    externalEx.getApiName(),
                    ex.getErrorCode(),
                    operationContext);
        } else if (ex instanceof AccessDeniedException) {
            AccessDeniedException accessEx = (AccessDeniedException) ex;
            errorResponse = ErrorContextBuilder.buildAccessDeniedError(
                    accessEx.getResource(),
                    "READ_WRITE",
                    operationContext);
        } else if (ex instanceof EmptyInstrumentsListException) {
            // Специальная обработка для пустых списков инструментов - возвращаем 200
            EmptyInstrumentsListException emptyEx = (EmptyInstrumentsListException) ex;
            errorResponse = ErrorContextBuilder.buildEmptySearchResultError(
                    Map.of("instrumentType", emptyEx.getInstrumentType()),
                    operationContext);
        } else {
            // Общая обработка для других кастомных исключений
            errorResponse = ApiErrorResponse.builder()
                    .message(ex.getMessage())
                    .errorCode(ex.getErrorCode())
                    .status(ex.getHttpStatus())
                    .path(requestPath)
                    .operationContext(operationContext)
                    .suggestion("Обратитесь к документации API или администратору системы")
                    .documentationUrl("/api/docs")
                    .build();
        }

        // Добавляем трассировку в dev режиме
        if (isDevMode()) {
            errorResponse.setTrace(getStackTrace(ex));
        }

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * Обработка ошибок валидации Spring
     *
     * @param ex      исключение валидации
     * @param request HTTP запрос
     * @return ответ с деталями ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Validation error: {}", ex.getMessage());

        List<ApiErrorResponse.ValidationError> validationErrors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(new ApiErrorResponse.ValidationError(
                    fieldError.getField(),
                    fieldError.getRejectedValue(),
                    fieldError.getDefaultMessage()));
        }

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .message("Ошибка валидации входных данных")
                .errorCode("VALIDATION_ERROR")
                .status(400)
                .path(getRequestPath(request))
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка ошибок парсинга JSON
     *
     * @param ex      исключение парсинга
     * @param request HTTP запрос
     * @return ответ об ошибке парсинга
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {

        logger.warn("JSON parsing error: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .message("Некорректный формат JSON в теле запроса")
                .errorCode("INVALID_JSON")
                .status(400)
                .path(getRequestPath(request))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка ошибок несоответствия типов параметров
     *
     * @param ex      исключение несоответствия типа
     * @param request HTTP запрос
     * @return ответ об ошибке типа
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        logger.warn("Type mismatch error: {}", ex.getMessage());

        String message = String.format("Параметр '%s' должен быть типа '%s', получен: '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getValue());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .message(message)
                .errorCode("TYPE_MISMATCH")
                .status(400)
                .path(getRequestPath(request))
                .details(Map.of(
                        "parameter", ex.getName(),
                        "expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                        "actualValue", ex.getValue()))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка отсутствующих обязательных параметров
     *
     * @param ex      исключение отсутствующего параметра
     * @param request HTTP запрос
     * @return ответ об отсутствующем параметре
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {

        logger.warn("Missing parameter error: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .message(String.format("Отсутствует обязательный параметр: %s", ex.getParameterName()))
                .errorCode("MISSING_PARAMETER")
                .status(400)
                .path(getRequestPath(request))
                .details(Map.of(
                        "parameter", ex.getParameterName(),
                        "parameterType", ex.getParameterType()))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка неподдерживаемых HTTP методов
     *
     * @param ex      исключение неподдерживаемого метода
     * @param request HTTP запрос
     * @return ответ о неподдерживаемом методе
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        logger.warn("Method not supported error: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .message(String.format("HTTP метод '%s' не поддерживается для данного endpoint", ex.getMethod()))
                .errorCode("METHOD_NOT_SUPPORTED")
                .status(405)
                .path(getRequestPath(request))
                .details(Map.of(
                        "method", ex.getMethod(),
                        "supportedMethods", ex.getSupportedMethods()))
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Обработка 404 ошибок (endpoint не найден)
     *
     * @param ex      исключение не найденного endpoint
     * @param request HTTP запрос
     * @return ответ о не найденном endpoint
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {

        logger.warn("No handler found error: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .message(String.format("Endpoint '%s %s' не найден", ex.getHttpMethod(), ex.getRequestURL()))
                .errorCode("ENDPOINT_NOT_FOUND")
                .status(404)
                .path(getRequestPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Обработка всех остальных исключений
     *
     * @param ex      исключение
     * @param request HTTP запрос
     * @return общий ответ об ошибке
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        String message = isDevMode() ? ex.getMessage() : "Внутренняя ошибка сервера";

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .message(message)
                .errorCode("INTERNAL_SERVER_ERROR")
                .status(500)
                .path(getRequestPath(request))
                .build();

        // Добавляем трассировку в dev режиме
        if (isDevMode()) {
            errorResponse.setTrace(getStackTrace(ex));
        }

        return ResponseEntity.internalServerError().body(errorResponse);
    }

    /**
     * Получить путь запроса из WebRequest
     *
     * @param request HTTP запрос
     * @return путь запроса
     */
    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    /**
     * Проверить, находится ли приложение в режиме разработки
     *
     * @return true если dev режим
     */
    private boolean isDevMode() {
        return "dev".equals(activeProfile) || "development".equals(activeProfile);
    }

    /**
     * Получить трассировку стека исключения
     *
     * @param ex исключение
     * @return трассировка стека
     */
    private String getStackTrace(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Определить контекст операции по пути запроса
     *
     * @param requestPath путь запроса
     * @return контекст операции
     */
    private String getOperationContext(String requestPath) {
        if (requestPath.contains("/api/cache/instruments")) {
            return "Получение списка всех инструментов";
        } else if (requestPath.contains("/api/cache/by-figi")) {
            return "Поиск инструмента по FIGI";
        } else if (requestPath.contains("/api/cache/shares")) {
            return "Получение списка акций";
        } else if (requestPath.contains("/api/cache/futures")) {
            return "Получение списка фьючерсов";
        } else if (requestPath.contains("/api/cache/indicatives")) {
            return "Получение списка индикативов";
        } else if (requestPath.contains("/api/cache/last-prices")) {
            return "Получение последних цен";
        } else if (requestPath.contains("/api/orders")) {
            return "Управление заявками";
        } else if (requestPath.contains("/api/cache/clear")) {
            return "Очистка кэша";
        } else if (requestPath.contains("/api/cache/force-update")) {
            return "Принудительное обновление кэша";
        } else {
            return "Неизвестная операция";
        }
    }

    /**
     * Получить ожидаемый формат для поля валидации
     *
     * @param field поле
     * @return ожидаемый формат
     */
    private String getExpectedFormat(String field) {
        switch (field.toLowerCase()) {
            case "figi":
                return "12-символьный код (буквы и цифры)";
            case "time":
                return "Время в формате HH:mm:ss";
            case "quantity":
                return "Положительное целое число";
            case "price":
                return "Положительное число с точностью до 2 знаков";
            case "orderid":
                return "UUID в формате xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
            default:
                return "Проверьте документацию API";
        }
    }
}
