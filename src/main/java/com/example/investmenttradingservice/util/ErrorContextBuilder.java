package com.example.investmenttradingservice.util;

import com.example.investmenttradingservice.DTO.ApiErrorResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Утилитный класс для создания детальных ошибок с контекстом для тестировщиков
 *
 * <p>
 * Предоставляет методы для создания информативных ошибок с подробным описанием,
 * рекомендациями по исправлению и примерами корректного использования.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class ErrorContextBuilder {

    /**
     * Создает детальную ошибку для не найденного инструмента
     *
     * @param figi             FIGI инструмента
     * @param operationContext контекст операции
     * @return детальная ошибка
     */
    public static ApiErrorResponse buildInstrumentNotFoundError(String figi, String operationContext) {
        return ApiErrorResponse.builder()
                .message(String.format("Инструмент с FIGI '%s' не найден в системе", figi))
                .errorCode("INSTRUMENT_NOT_FOUND")
                .status(404)
                .operationContext(operationContext)
                .suggestion(
                        "Проверьте корректность FIGI. Используйте GET /api/cache/instruments для получения списка доступных инструментов")
                .examples(Arrays.asList(
                        "BBG000B9XRY4", // SBER
                        "BBG004730N88", // GAZP
                        "BBG004730ZJ9" // LKOH
                ))
                .documentationUrl("/api/docs/instruments")
                .details(Map.of(
                        "figi", figi,
                        "searchScope", "all_instruments",
                        "availableEndpoints", Arrays.asList(
                                "GET /api/cache/instruments",
                                "GET /api/cache/shares",
                                "GET /api/cache/futures")))
                .build();
    }

    /**
     * Создает детальную ошибку валидации
     *
     * @param field            поле, которое не прошло валидацию
     * @param rejectedValue    отклоненное значение
     * @param expectedFormat   ожидаемый формат
     * @param operationContext контекст операции
     * @return детальная ошибка валидации
     */
    public static ApiErrorResponse buildValidationError(String field, Object rejectedValue,
            String expectedFormat, String operationContext) {
        return ApiErrorResponse.builder()
                .message(String.format("Поле '%s' содержит некорректное значение: '%s'", field, rejectedValue))
                .errorCode("VALIDATION_ERROR")
                .status(400)
                .operationContext(operationContext)
                .suggestion(String.format("Исправьте поле '%s' согласно требованиям: %s", field, expectedFormat))
                .examples(generateExamples(field))
                .documentationUrl("/api/docs/validation")
                .details(Map.of(
                        "field", field,
                        "rejectedValue", rejectedValue,
                        "expectedFormat", expectedFormat,
                        "validationRules", getValidationRules(field)))
                .build();
    }

    /**
     * Создает детальную ошибку для неподдерживаемого типа данных
     *
     * @param unsupportedType  неподдерживаемый тип
     * @param supportedTypes   поддерживаемые типы
     * @param operationContext контекст операции
     * @return детальная ошибка
     */
    public static ApiErrorResponse buildUnsupportedTypeError(String unsupportedType,
            List<String> supportedTypes,
            String operationContext) {
        return ApiErrorResponse.builder()
                .message(String.format("Тип '%s' не поддерживается", unsupportedType))
                .errorCode("UNSUPPORTED_TYPE")
                .status(400)
                .operationContext(operationContext)
                .suggestion(String.format("Используйте один из поддерживаемых типов: %s",
                        String.join(", ", supportedTypes)))
                .examples(supportedTypes)
                .documentationUrl("/api/docs/instrument-types")
                .details(Map.of(
                        "unsupportedType", unsupportedType,
                        "supportedTypes", supportedTypes,
                        "totalSupportedTypes", supportedTypes.size()))
                .build();
    }

    /**
     * Создает детальную ошибку для пустого результата поиска
     *
     * @param searchCriteria   критерии поиска
     * @param operationContext контекст операции
     * @return детальная ошибка
     */
    public static ApiErrorResponse buildEmptySearchResultError(Map<String, Object> searchCriteria,
            String operationContext) {
        return ApiErrorResponse.builder()
                .message("Поиск не дал результатов")
                .errorCode("EMPTY_SEARCH_RESULT")
                .status(200) // Согласно требованиям - 200 с пустым списком
                .operationContext(operationContext)
                .suggestion("Попробуйте изменить критерии поиска или проверьте доступность данных")
                .examples(Arrays.asList(
                        "Используйте GET /api/cache/instruments для получения всех инструментов",
                        "Проверьте актуальность данных в кэше"))
                .documentationUrl("/api/docs/search")
                .details(Map.of(
                        "searchCriteria", searchCriteria,
                        "suggestion", "Попробуйте более широкие критерии поиска"))
                .build();
    }

    /**
     * Создает детальную ошибку для ошибок внешнего API
     *
     * @param apiName          название внешнего API
     * @param errorCode        код ошибки от внешнего API
     * @param operationContext контекст операции
     * @return детальная ошибка
     */
    public static ApiErrorResponse buildExternalApiError(String apiName, String errorCode,
            String operationContext) {
        return ApiErrorResponse.builder()
                .message(String.format("Ошибка внешнего API '%s': %s", apiName, errorCode))
                .errorCode("EXTERNAL_API_ERROR")
                .status(502)
                .operationContext(operationContext)
                .suggestion("Попробуйте повторить запрос позже или обратитесь к администратору")
                .examples(Arrays.asList(
                        "Проверьте статус внешнего API",
                        "Убедитесь в корректности токенов доступа"))
                .documentationUrl("/api/docs/external-apis")
                .details(Map.of(
                        "apiName", apiName,
                        "externalErrorCode", errorCode,
                        "retryRecommended", true))
                .build();
    }

    /**
     * Создает детальную ошибку для ошибок доступа
     *
     * @param resource           ресурс, к которому запрещен доступ
     * @param requiredPermission требуемое разрешение
     * @param operationContext   контекст операции
     * @return детальная ошибка
     */
    public static ApiErrorResponse buildAccessDeniedError(String resource, String requiredPermission,
            String operationContext) {
        return ApiErrorResponse.builder()
                .message(String.format("Доступ к ресурсу '%s' запрещен", resource))
                .errorCode("ACCESS_DENIED")
                .status(403)
                .operationContext(operationContext)
                .suggestion(
                        String.format("Обратитесь к администратору для получения разрешения: %s", requiredPermission))
                .examples(Arrays.asList(
                        "Проверьте токен авторизации",
                        "Убедитесь в наличии необходимых разрешений"))
                .documentationUrl("/api/docs/authentication")
                .details(Map.of(
                        "resource", resource,
                        "requiredPermission", requiredPermission,
                        "authenticationRequired", true))
                .build();
    }

    /**
     * Генерирует примеры для поля валидации
     *
     * @param field поле
     * @return список примеров
     */
    private static List<String> generateExamples(String field) {
        switch (field.toLowerCase()) {
            case "figi":
                return Arrays.asList("BBG000B9XRY4", "BBG004730N88", "BBG004730ZJ9");
            case "time":
                return Arrays.asList("09:30:00", "10:15:30", "18:45:00");
            case "quantity":
                return Arrays.asList("1", "10", "100");
            case "price":
                return Arrays.asList("100.50", "2500.00", "0.01");
            default:
                return Arrays.asList("Проверьте документацию API");
        }
    }

    /**
     * Получает правила валидации для поля
     *
     * @param field поле
     * @return правила валидации
     */
    private static Map<String, Object> getValidationRules(String field) {
        switch (field.toLowerCase()) {
            case "figi":
                return Map.of(
                        "pattern", "^[A-Z0-9]{12}$",
                        "minLength", 12,
                        "maxLength", 12,
                        "description", "FIGI должен содержать 12 символов (буквы и цифры)");
            case "time":
                return Map.of(
                        "pattern", "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$",
                        "format", "HH:mm:ss",
                        "description", "Время в формате ЧЧ:ММ:СС");
            case "quantity":
                return Map.of(
                        "min", 1,
                        "type", "integer",
                        "description", "Количество должно быть положительным целым числом");
            case "price":
                return Map.of(
                        "min", 0.01,
                        "type", "decimal",
                        "precision", 2,
                        "description", "Цена должна быть положительным числом с точностью до 2 знаков");
            default:
                return Map.of("description", "Проверьте документацию API");
        }
    }
}
