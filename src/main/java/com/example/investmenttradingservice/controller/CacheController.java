package com.example.investmenttradingservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.investmenttradingservice.DTO.ClosePriceDTO;
import com.example.investmenttradingservice.DTO.ClosePriceEveningSessionDTO;
import com.example.investmenttradingservice.DTO.FutureDTO;
import com.example.investmenttradingservice.DTO.IndicativeDTO;
import com.example.investmenttradingservice.DTO.OpenPriceDTO;
import com.example.investmenttradingservice.DTO.ShareDTO;
import com.example.investmenttradingservice.DTO.LastPriceDTO;
import com.example.investmenttradingservice.DTO.DividendDto;
import com.example.investmenttradingservice.DTO.LimitsDto;
import com.example.investmenttradingservice.DTO.ApiSuccessResponse;
import com.example.investmenttradingservice.exception.EmptyInstrumentsListException;
import com.example.investmenttradingservice.exception.InstrumentNotFoundException;
import com.example.investmenttradingservice.exception.ValidationException;
import com.example.investmenttradingservice.service.CacheService;
import com.example.investmenttradingservice.service.InstrumentServiceFacade;
import com.example.investmenttradingservice.util.ApiResponseBuilder;

/**
 * REST контроллер для управления кэшем
 *
 * <p>
 * Предоставляет API endpoints для управления кэшем системы,
 * включая очистку, принудительное обновление и получение данных из кэша.
 * </p>
 *
 * <p>
 * Основные операции:
 * </p>
 * <ul>
 * <li>Очистка кэша</li>
 * <li>Принудительное обновление кэша</li>
 * <li>Получение инструментов из кэша</li>
 * <li>Получение цен из кэша</li>
 * <li>Получение лимитов из кэша</li>
 * <li>Синхронное обновление лимитов в кэше с задержкой и retry</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    /** Логгер для записи операций контроллера */
    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    /** Фасад для работы со всеми типами инструментов */
    private final InstrumentServiceFacade instrumentServiceFacade;

    /** Сервис для управления кэшем */
    private final CacheService cacheService;

    /**
     * Конструктор контроллера кэша
     *
     * @param instrumentServiceFacade фасад для работы со всеми типами инструментов
     * @param cacheService            сервис для управления кэшем
     */
    public CacheController(InstrumentServiceFacade instrumentServiceFacade, CacheService cacheService) {
        this.instrumentServiceFacade = instrumentServiceFacade;
        this.cacheService = cacheService;
    }

    /**
     * Очистка всех кэшей
     *
     * <p>
     * Очищает все кэши системы (sharesCache, futuresCache и т.д.).
     * В случае ошибки возвращает соответствующий статус и сообщение.
     * </p>
     *
     * <p>
     * Пример успешного ответа:
     * </p>
     * 
     * <pre>{@code
     * {
     *   "message": "Кэш успешно очищен",
     *   "status": "success",
     *   "timestamp": 1234567890
     * }
     * }</pre>
     *
     * <p>
     * Пример ответа с ошибкой:
     * </p>
     * 
     * <pre>{@code
     * {
     *   "message": "Ошибка при очистке кэша: ...",
     *   "status": "error",
     *   "timestamp": 1234567890
     * }
     * }</pre>
     *
     * @return ResponseEntity с результатом операции
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        logger.info("Получен запрос на очистку кэша");
        Map<String, Object> response = new HashMap<>();

        try {
            cacheService.clearAllCaches();

            response.put("message", "Кэш успешно очищен");
            response.put("status", "success");
            response.put("timestamp", System.currentTimeMillis());

            logger.info("Кэш успешно очищен через API");
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            logger.error("Ошибка при очистке кэша через API: {}", e.getMessage(), e);

            response.put("message", "Ошибка при очистке кэша: " + e.getMessage());
            response.put("status", "error");
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/force-update")
    public ResponseEntity<Map<String, Object>> forceUpdateCache() {
        Map<String, Object> response = new HashMap<>();
        try {
            cacheService.manualWarmupCache();
            response.put("message", "Кэш успешно принудительно обновлен");
            response.put("status", "success");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            logger.error("Ошибка при принудительном обновлении кэша: {}", e.getMessage(), e);
            response.put("message", "Ошибка при принудительном обновлении кэша: " + e.getMessage());
            response.put("status", "error");
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }

    }

    /**
     * Получение всех инструментов ТОЛЬКО из кэша
     *
     * <p>
     * Этот эндпоинт возвращает данные исключительно из кэша без обращения к базе
     * данных.
     * Если кэш пуст, соответствующие списки будут пустыми. Это обеспечивает быстрый
     * отклик
     * и не создает дополнительную нагрузку на базу данных.
     * </p>
     *
     * <p>
     * Особенности:
     * </p>
     * <ul>
     * <li>Работает только с кэшированными данными</li>
     * <li>Быстрый отклик без обращения к БД</li>
     * <li>Может возвращать пустые списки, если кэш не заполнен</li>
     * <li>Включает общее количество инструментов</li>
     * </ul>
     *
     * <p>
     * Пример успешного ответа:
     * </p>
     * 
     * <pre>{@code
     * {
     *   "success": true,
     *   "status": "success",
     *   "message": "Инструменты успешно получены из кэша",
     *   "shares_size": 169,
     *   "futures_size": 365,
     *   "indicatives_size": 57,
     *   "total_instruments": 591,
     *   "shares": [...],
     *   "futures": [...],
     *   "indicatives": [...],
     *   "timestamp": "2024-01-15T12:00:00"
     * }
     * }</pre>
     *
     * @return ResponseEntity с инструментами из кэша
     */
    @GetMapping("/instruments")
    public ResponseEntity<Object> getInstruments() {
        logger.info("Получен запрос на инструменты только из кэша");

        try {
            Map<String, Object> cacheData = instrumentServiceFacade.getAllInstrumentsFromCacheOnly();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("status", "success");
            response.put("message", "Инструменты успешно получены из кэша");
            response.put("shares_size", cacheData.get("shares_size"));
            response.put("futures_size", cacheData.get("futures_size"));
            response.put("indicatives_size", cacheData.get("indicatives_size"));
            response.put("total_instruments", cacheData.get("total_instruments"));
            response.put("shares", cacheData.get("shares"));
            response.put("futures", cacheData.get("futures"));
            response.put("indicatives", cacheData.get("indicatives"));
            response.put("closePrices_size", cacheData.get("closePrices_size"));
            response.put("openPrices_size", cacheData.get("openPrices_size"));
            response.put("closePriceEveningSessions_size", cacheData.get("closePriceEveningSessions_size"));
            response.put("lastPrices_size", cacheData.get("lastPrices_size"));
            response.put("dividends_size", cacheData.get("dividends_size"));
            response.put("closePriceEveningSessions", cacheData.get("closePriceEveningSessions"));
            response.put("openPrices", cacheData.get("openPrices"));
            response.put("lastPrices", cacheData.get("lastPrices"));
            response.put("dividends", cacheData.get("dividends"));
            response.put("timestamp", LocalDateTime.now());

            logger.info(
                    "Инструменты успешно получены из кэша: {} акций, {} фьючерсов, {} индикативов, {} цен последних сделок, {} дивидендов",
                    cacheData.get("shares_size"), cacheData.get("futures_size"), cacheData.get("indicatives_size"),
                    cacheData.get("lastPrices_size"), cacheData.get("dividends_size"));

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            logger.error("Ошибка при получении инструментов из кэша: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("status", "error");
            errorResponse.put("message", "Ошибка при получении инструментов из кэша: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Получение акций ТОЛЬКО из кэша
     *
     * <p>
     * Этот эндпоинт возвращает акции исключительно из кэша без обращения к базе
     * данных.
     * Если кэш пуст, возвращается 200 с пустым списком и соответствующим
     * сообщением.
     * </p>
     *
     * @return ResponseEntity с акциями из кэша
     */
    @GetMapping("/shares")
    public ResponseEntity<ApiSuccessResponse<List<ShareDTO>>> getShares() {
        logger.info("Получен запрос на акции только из кэша");

        try {
            List<ShareDTO> shares = instrumentServiceFacade.getSharesFromCacheOnly();

            if (shares.isEmpty()) {
                throw new EmptyInstrumentsListException(
                        "Акции не найдены в кэше. Возможно, кэш еще не заполнен или произошла ошибка при загрузке данных.",
                        "shares");
            }

            logger.info("Акции успешно получены из кэша: {} записей", shares.size());

            ApiSuccessResponse<List<ShareDTO>> response = ApiSuccessResponse.<List<ShareDTO>>builder()
                    .message("Акции успешно получены из кэша")
                    .data(shares)
                    .totalCount(shares.size())
                    .dataType("shares")
                    .addMetadata("cacheStatus", "active")
                    .build();

            return ResponseEntity.ok(response);

        } catch (EmptyInstrumentsListException e) {
            // Это исключение обрабатывается глобальным обработчиком и возвращает 200
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении акций из кэша: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении акций", e);
        }
    }

    /**
     * Получение фьючерсов ТОЛЬКО из кэша
     *
     * <p>
     * Этот эндпоинт возвращает фьючерсы исключительно из кэша без обращения к базе
     * данных.
     * Если кэш пуст, возвращается пустой список.
     * </p>
     *
     * @return ResponseEntity с фьючерсами из кэша
     */
    @GetMapping("/futures")
    public ResponseEntity<Object> getFutures() {
        logger.info("Получен запрос на фьючерсы только из кэша");

        try {
            List<FutureDTO> futures = instrumentServiceFacade.getFuturesFromCacheOnly();
            logger.info("Фьючерсы успешно получены из кэша: {} записей", futures.size());
            return ApiResponseBuilder.success("Фьючерсы успешно получены из кэша", futures, futures.size(), "futures");
        } catch (Exception e) {
            logger.error("Ошибка при получении фьючерсов из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении фьючерсов из кэша", e);
        }
    }

    /**
     * Получение индикативов ТОЛЬКО из кэша
     *
     * <p>
     * Этот эндпоинт возвращает индикативы исключительно из кэша без обращения к
     * базе данных.
     * Если кэш пуст, возвращается пустой список.
     * </p>
     *
     * @return ResponseEntity с индикативами из кэша
     */
    @GetMapping("/indicatives")
    public ResponseEntity<Object> getIndicatives() {
        logger.info("Получен запрос на индикативы только из кэша");

        try {
            List<IndicativeDTO> indicatives = instrumentServiceFacade.getIndicativesFromCacheOnly();
            logger.info("Индикативы успешно получены из кэша: {} записей", indicatives.size());
            return ApiResponseBuilder.success("Индикативы успешно получены из кэша", indicatives, indicatives.size(),
                    "indicatives");
        } catch (Exception e) {
            logger.error("Ошибка при получении индикативов из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении индикативов из кэша", e);
        }
    }

    /**
     * Получение цен закрытия ТОЛЬКО из кэша
     *
     * <p>
     * Этот эндпоинт возвращает цены закрытия исключительно из кэша без обращения к
     * базе данных.
     * Если кэш пуст, возвращается пустой список.
     * </p>
     *
     * @return ResponseEntity с ценами закрытия из кэша
     */
    @GetMapping("/close-prices")
    public ResponseEntity<Object> getClosePrices() {
        logger.info("Получен запрос на цены закрытия только из кэша");

        try {
            List<ClosePriceDTO> closePrices = instrumentServiceFacade.getClosePricesFromCacheOnly();
            logger.info("Цены закрытия успешно получены из кэша: {} записей", closePrices.size());
            return ApiResponseBuilder.success("Цены закрытия успешно получены из кэша", closePrices, closePrices.size(),
                    "close_prices");
        } catch (Exception e) {
            logger.error("Ошибка при получении цен закрытия из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении цен закрытия из кэша", e);
        }
    }

    @GetMapping("/open-prices")
    public ResponseEntity<Object> getOpenPrices() {
        logger.info("Получен запрос на цены открытия только из кэша");

        try {
            List<OpenPriceDTO> openPrices = instrumentServiceFacade.getOpenPricesFromCacheOnly();
            logger.info("Цены открытия успешно получены из кэша: {} записей", openPrices.size());
            return ApiResponseBuilder.success("Цены открытия успешно получены из кэша", openPrices, openPrices.size(),
                    "open_prices");
        } catch (Exception e) {
            logger.error("Ошибка при получении цен открытия из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении цен открытия из кэша", e);
        }
    }

    @GetMapping("/close-price-evening-sessions")
    public ResponseEntity<Object> getClosePriceEveningSessions() {
        try {
            List<ClosePriceEveningSessionDTO> closePriceEveningSessions = instrumentServiceFacade
                    .getClosePriceEveningSessionsFromCacheOnly();
            logger.info("Получен запрос на цены закрытия вечерней сессии только из кэша");
            return ApiResponseBuilder.success("Цены закрытия вечерней сессии успешно получены из кэша",
                    closePriceEveningSessions, closePriceEveningSessions.size(),
                    "close_price_evening_sessions");
        } catch (Exception e) {
            logger.error("Ошибка при получении цен закрытия вечерней сессии из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении цен закрытия вечерней сессии из кэша", e);
        }
    }

    /**
     * Получение дивидендов ТОЛЬКО из кэша
     *
     * <p>
     * Этот эндпоинт возвращает дивиденды исключительно из кэша без обращения к базе
     * данных.
     * Если кэш пуст, возвращается пустой список.
     * </p>
     *
     * @return ResponseEntity с дивидендами из кэша
     */
    @GetMapping("/dividends")
    public ResponseEntity<Object> getDividends() {
        logger.info("Получен запрос на дивиденды только из кэша");

        try {
            List<DividendDto> dividends = instrumentServiceFacade.getDividendsFromCacheOnly();
            logger.info("Дивиденды успешно получены из кэша: {} записей", dividends.size());
            return ApiResponseBuilder.success("Дивиденды успешно получены из кэша", dividends, dividends.size(),
                    "dividends");
        } catch (Exception e) {
            logger.error("Ошибка при получении дивидендов из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении дивидендов из кэша", e);
        }
    }

    /**
     * Получение последних цен ТОЛЬКО из кэша
     *
     * <p>
     * Этот эндпоинт возвращает последние цены инструментов исключительно из кэша
     * без обращения к базе
     * данных.
     * Если кэш пуст, возвращается пустой список.
     * </p>
     *
     * @return ResponseEntity с последними ценами из кэша
     */
    @GetMapping("/last-prices")
    public ResponseEntity<Object> getLastPrices() {
        logger.info("Получен запрос на последние цены только из кэша");

        try {
            List<LastPriceDTO> lastPrices = instrumentServiceFacade.getLastPricesFromCacheOnly();
            logger.info("Последние цены успешно получены из кэша: {} записей", lastPrices.size());
            return ApiResponseBuilder.success("Последние цены успешно получены из кэша", lastPrices, lastPrices.size(),
                    "last_prices");
        } catch (Exception e) {
            logger.error("Ошибка при получении последних цен из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении последних цен из кэша", e);
        }
    }

    /**
     * Получение информации об инструменте по FIGI из кэша
     *
     * <p>
     * Этот эндпоинт выполняет поиск инструмента по FIGI во всех типах кэшей:
     * акции, фьючерсы, индикативы, цены закрытия, цены открытия, цены последних
     * сделок и дивиденды.
     * Возвращает все найденные записи, связанные с указанным FIGI.
     * </p>
     *
     * <p>
     * Особенности:
     * </p>
     * <ul>
     * <li>Поиск во всех типах инструментов</li>
     * <li>Возвращает объединенный результат</li>
     * <li>Включает fallback на базу данных</li>
     * <li>Логирование результатов поиска</li>
     * </ul>
     *
     * @param figi идентификатор инструмента для поиска
     * @return ResponseEntity с найденными записями
     */
    @GetMapping("/by-figi/{figi}")
    public ResponseEntity<ApiSuccessResponse<List<Object>>> getInstrumentByFigi(@PathVariable String figi) {
        logger.info("Получен запрос на поиск инструмента по FIGI: {}", figi);

        // Валидация FIGI
        if (figi == null || figi.trim().isEmpty()) {
            throw new ValidationException(
                    "FIGI не может быть пустым",
                    "figi",
                    figi);
        }

        if (!figi.matches("^[A-Z0-9]{12}$")) {
            throw new ValidationException(
                    "FIGI должен содержать ровно 12 символов (буквы и цифры)",
                    "figi",
                    figi);
        }

        try {
            List<Object> instruments = instrumentServiceFacade.getInstrumentByFigi(figi);

            if (instruments.isEmpty()) {
                // Инструмент не найден - возвращаем 404
                throw new InstrumentNotFoundException(
                        String.format("Инструмент с FIGI '%s' не найден в системе", figi),
                        figi);
            }

            logger.info("Найдено {} записей для FIGI: {}", instruments.size(), figi);

            ApiSuccessResponse<List<Object>> response = ApiSuccessResponse.<List<Object>>builder()
                    .message(String.format("Инструмент найден по FIGI: %s", figi))
                    .data(instruments)
                    .totalCount(instruments.size())
                    .dataType("instruments")
                    .addMetadata("figi", figi)
                    .addMetadata("searchScope", "all_instruments")
                    .build();

            return ResponseEntity.ok(response);

        } catch (InstrumentNotFoundException | ValidationException e) {
            // Эти исключения обрабатываются глобальным обработчиком
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при поиске инструмента по FIGI {}: {}", figi, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при поиске инструмента", e);
        }
    }

    /**
     * Получение информации об инструменте по FIGI ТОЛЬКО из кэша
     *
     * <p>
     * Этот эндпоинт выполняет поиск инструмента по FIGI только в кэшированных
     * данных.
     * Не обращается к базе данных, поэтому работает быстрее.
     * </p>
     *
     * @param figi идентификатор инструмента для поиска
     * @return ResponseEntity с найденными записями из кэша
     */
    @GetMapping("/by-figi-cache-only/{figi}")
    public ResponseEntity<ApiSuccessResponse<List<Object>>> getInstrumentByFigiFromCacheOnly(
            @PathVariable String figi) {
        logger.info("Получен запрос на поиск инструмента по FIGI только в кэше: {}", figi);

        // Валидация FIGI
        if (figi == null || figi.trim().isEmpty()) {
            throw new ValidationException(
                    "FIGI не может быть пустым",
                    "figi",
                    figi);
        }

        if (!figi.matches("^[A-Z0-9]{12}$")) {
            throw new ValidationException(
                    "FIGI должен содержать ровно 12 символов (буквы и цифры)",
                    "figi",
                    figi);
        }

        try {
            List<Object> instruments = instrumentServiceFacade.getInstrumentByFigiFromCacheOnly(figi);

            if (instruments.isEmpty()) {
                // Инструмент не найден в кэше - возвращаем 404
                throw new InstrumentNotFoundException(
                        String.format("Инструмент с FIGI '%s' не найден в кэше", figi),
                        figi);
            }

            logger.info("Найдено {} записей в кэше для FIGI: {}", instruments.size(), figi);

            ApiSuccessResponse<List<Object>> response = ApiSuccessResponse.<List<Object>>builder()
                    .message(String.format("Инструмент найден в кэше по FIGI: %s", figi))
                    .data(instruments)
                    .totalCount(instruments.size())
                    .dataType("instruments")
                    .addMetadata("figi", figi)
                    .addMetadata("searchScope", "cache_only")
                    .addMetadata("cacheOnly", true)
                    .build();

            return ResponseEntity.ok(response);

        } catch (InstrumentNotFoundException | ValidationException e) {
            // Эти исключения обрабатываются глобальным обработчиком
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при поиске инструмента по FIGI в кэше {}: {}", figi, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при поиске инструмента в кэше", e);
        }
    }

    /**
     * Получение статистики кэша для диагностики
     *
     * <p>
     * Возвращает подробную информацию о состоянии всех кэшей системы,
     * включая количество записей и используемые ключи.
     * </p>
     *
     *
     * @return ResponseEntity с статистикой кэша
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        logger.info("Получен запрос на статистику кэша");
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> statistics = cacheService.getCacheStats();

            response.put("message", "Статистика кэша получена");
            response.put("status", "success");
            response.put("cache_statistics", statistics);
            response.put("timestamp", LocalDateTime.now());

            logger.info("Статистика кэша успешно получена через API");
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            logger.error("Ошибка при получении статистики кэша через API: {}", e.getMessage(), e);

            response.put("message", "Ошибка при получении статистики кэша: " + e.getMessage());
            response.put("status", "error");
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ===========================================
    // Методы для работы с лимитами цен (limits)
    // ===========================================

    /**
     * Получение лимитов для одного инструмента по FIGI из кэша
     *
     * <p>
     * Этот эндпоинт возвращает лимиты цены (верхний и нижний) для конкретного
     * инструмента из кэша без обращения к API.
     * </p>
     *
     * @param figi идентификатор инструмента для поиска лимитов
     * @return ResponseEntity с лимитами инструмента
     */
    @GetMapping("/limits/{figi}")
    public ResponseEntity<ApiSuccessResponse<LimitsDto>> getLimitByFigi(@PathVariable String figi) {
        logger.info("Получен запрос на лимиты для FIGI: {}", figi);

        // Валидация FIGI
        if (figi == null || figi.trim().isEmpty()) {
            throw new ValidationException(
                    "FIGI не может быть пустым",
                    "figi",
                    figi);
        }

        if (!figi.matches("^[A-Z0-9]{12}$")) {
            throw new ValidationException(
                    "FIGI должен содержать ровно 12 символов (буквы и цифры)",
                    "figi",
                    figi);
        }

        try {
            LimitsDto limit = instrumentServiceFacade.getLimitByInstrumentIdFromCache(figi);

            if (limit == null) {
                throw new InstrumentNotFoundException(
                        String.format("Лимиты для инструмента с FIGI '%s' не найдены в кэше", figi),
                        figi);
            }

            logger.info("Лимиты для FIGI {} найдены в кэше", figi);

            ApiSuccessResponse<LimitsDto> response = ApiSuccessResponse.<LimitsDto>builder()
                    .message(String.format("Лимиты для инструмента %s получены из кэша", figi))
                    .data(limit)
                    .totalCount(1)
                    .dataType("limits")
                    .addMetadata("figi", figi)
                    .addMetadata("cacheStatus", "active")
                    .build();

            return ResponseEntity.ok(response);

        } catch (InstrumentNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении лимитов для FIGI {}: {}", figi, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении лимитов", e);
        }
    }

    /**
     * Получение всех лимитов из кэша
     *
     * <p>
     * Этот эндпоинт возвращает лимиты для всех инструментов, которые есть в кэше
     * лимитов.
     * Возвращает полный список всех доступных лимитов без фильтрации.
     * </p>
     *
     * @return ResponseEntity с полным списком лимитов из кэша
     */
    @GetMapping("/limits")
    public ResponseEntity<ApiSuccessResponse<List<LimitsDto>>> getAllLimits() {
        logger.info("Получен запрос на все лимиты из кэша");

        try {
            List<LimitsDto> allLimits = instrumentServiceFacade.getLimitsFromCacheOnly();

            logger.info("Получено {} лимитов из кэша", allLimits.size());

            ApiSuccessResponse<List<LimitsDto>> response = ApiSuccessResponse.<List<LimitsDto>>builder()
                    .message(String.format("Получено %d лимитов из кэша", allLimits.size()))
                    .data(allLimits)
                    .totalCount(allLimits.size())
                    .dataType("limits")
                    .addMetadata("cacheStatus", "active")
                    .addMetadata("source", "cache")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении всех лимитов из кэша: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении лимитов", e);
        }
    }

    /**
     * Обновление всех лимитов в кэше с задержкой и retry-механизмом
     *
     * <p>
     * Этот эндпоинт запускает обновление лимитов для всех инструментов
     * (акций и фьючерсов) из кэша с задержкой 100ms между запросами и
     * retry-механизмом
     * для соблюдения rate limits API. Возвращает результат после завершения
     * операции.
     * </p>
     *
     * @return ResponseEntity с результатом обновления
     */
    @PostMapping("/limits/refresh")
    public ResponseEntity<Map<String, Object>> refreshLimits() {
        logger.info("Получен запрос на обновление лимитов в кэше");
        Map<String, Object> response = new HashMap<>();

        try {
            long startTime = System.currentTimeMillis();
            int count = instrumentServiceFacade.refreshAllLimitsSync();
            long duration = System.currentTimeMillis() - startTime;

            response.put("message",
                    String.format("Обновление лимитов завершено успешно. Обновлено записей: %d", count));
            response.put("status", "success");
            response.put("updatedCount", count);
            response.put("durationMs", duration);
            response.put("timestamp", LocalDateTime.now());

            logger.info("Обновление лимитов завершено через API: {} записей за {}ms", count, duration);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при обновлении лимитов через API: {}", e.getMessage(), e);

            response.put("message", "Ошибка при обновлении лимитов: " + e.getMessage());
            response.put("status", "error");
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(response);
        }
    }

}
