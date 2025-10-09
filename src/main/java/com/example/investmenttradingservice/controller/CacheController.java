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
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.service.CacheInstrumentsService;
import com.example.investmenttradingservice.service.CacheService;
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

    /** Сервис для работы с кэшированными инструментами */
    private final CacheInstrumentsService cacheInstrumentsService;

    /** Сервис для управления кэшем */
    private final CacheService cacheService;

    /**
     * Конструктор контроллера кэша
     *
     * @param cacheInstrumentsService сервис для работы с кэшированными
     *                                инструментами
     * @param cacheService            сервис для управления кэшем
     */
    public CacheController(CacheInstrumentsService cacheInstrumentsService, CacheService cacheService) {
        this.cacheInstrumentsService = cacheInstrumentsService;
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
            Map<String, Object> cacheData = cacheInstrumentsService.getAllInstrumentsFromCacheOnly();

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
     * Если кэш пуст, возвращается пустой список.
     * </p>
     *
     * @return ResponseEntity с акциями из кэша
     */
    @GetMapping("/shares")
    public ResponseEntity<Object> getShares() {
        logger.info("Получен запрос на акции только из кэша");

        try {
            List<ShareDTO> shares = cacheInstrumentsService.getSharesFromCacheOnly();
            logger.info("Акции успешно получены из кэша: {} записей", shares.size());
            return ApiResponseBuilder.success("Акции успешно получены из кэша", shares, shares.size(), "shares");
        } catch (Exception e) {
            logger.error("Ошибка при получении акций из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении акций из кэша", e);
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
            List<FutureDTO> futures = cacheInstrumentsService.getFuturesFromCacheOnly();
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
            List<IndicativeDTO> indicatives = cacheInstrumentsService.getIndicativesFromCacheOnly();
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
            List<ClosePriceDTO> closePrices = cacheInstrumentsService.getClosePricesFromCacheOnly();
            LocalDateTime requestDate = cacheInstrumentsService
                    .getRequestDateForInstrument(InstrumentType.CLOSE_PRICES);
            logger.info("Цены закрытия успешно получены из кэша: {} записей", closePrices.size());
            return ApiResponseBuilder.success("Цены закрытия успешно получены из кэша", closePrices, closePrices.size(),
                    "close_prices", requestDate);
        } catch (Exception e) {
            logger.error("Ошибка при получении цен закрытия из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении цен закрытия из кэша", e);
        }
    }

    @GetMapping("/open-prices")
    public ResponseEntity<Object> getOpenPrices() {
        logger.info("Получен запрос на цены открытия только из кэша");

        try {
            List<OpenPriceDTO> openPrices = cacheInstrumentsService.getOpenPricesFromCacheOnly();
            LocalDateTime requestDate = cacheInstrumentsService.getRequestDateForInstrument(InstrumentType.OPEN_PRICES);
            logger.info("Цены открытия успешно получены из кэша: {} записей", openPrices.size());
            return ApiResponseBuilder.success("Цены открытия успешно получены из кэша", openPrices, openPrices.size(),
                    "open_prices", requestDate);
        } catch (Exception e) {
            logger.error("Ошибка при получении цен открытия из кэша: {}", e.getMessage(), e);
            return ApiResponseBuilder.error("Ошибка при получении цен открытия из кэша", e);
        }
    }

    @GetMapping("/close-price-evening-sessions")
    public ResponseEntity<Object> getClosePriceEveningSessions() {
        try {
            List<ClosePriceEveningSessionDTO> closePriceEveningSessions = cacheInstrumentsService
                    .getClosePriceEveningSessionsFromCacheOnly();
            LocalDateTime requestDate = cacheInstrumentsService
                    .getRequestDateForInstrument(InstrumentType.CLOSE_PRICES_EVENING_SESSION);
            logger.info("Получен запрос на цены закрытия вечерней сессии только из кэша");
            return ApiResponseBuilder.success("Цены закрытия вечерней сессии успешно получены из кэша",
                    closePriceEveningSessions, closePriceEveningSessions.size(),
                    "close_price_evening_sessions", requestDate);
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
            List<DividendDto> dividends = cacheInstrumentsService.getDividendsFromCacheOnly();
            LocalDateTime requestDate = cacheInstrumentsService.getRequestDateForInstrument(InstrumentType.DIVIDENDS);
            logger.info("Дивиденды успешно получены из кэша: {} записей", dividends.size());
            return ApiResponseBuilder.success("Дивиденды успешно получены из кэша", dividends, dividends.size(),
                    "dividends", requestDate);
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
            List<LastPriceDTO> lastPrices = cacheInstrumentsService.getLastPricesFromCacheOnly();
            LocalDateTime requestDate = cacheInstrumentsService.getRequestDateForInstrument(InstrumentType.LAST_PRICES);
            logger.info("Последние цены успешно получены из кэша: {} записей", lastPrices.size());
            return ApiResponseBuilder.success("Последние цены успешно получены из кэша", lastPrices, lastPrices.size(),
                    "last_prices", requestDate);
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
    public ResponseEntity<Object> getInstrumentByFigi(@PathVariable String figi) {
        logger.info("Получен запрос на поиск инструмента по FIGI: {}", figi);

        try {
            List<Object> instruments = cacheInstrumentsService.getInstrumentByFigi(figi);
            logger.info("Найдено {} записей для FIGI: {}", instruments.size(), figi);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("status", "success");
            response.put("message", "Инструмент найден по FIGI: " + figi);
            response.put("figi", figi);
            response.put("total_found", instruments.size());
            response.put("instruments", instruments);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            logger.error("Ошибка при поиске инструмента по FIGI {}: {}", figi, e.getMessage(), e);

            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("status", "error");
            errorResponse.put("message", "Ошибка при поиске инструмента по FIGI: " + e.getMessage());
            errorResponse.put("figi", figi);
            errorResponse.put("error", e.getClass().getSimpleName());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorResponse);
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
    public ResponseEntity<Object> getInstrumentByFigiFromCacheOnly(@PathVariable String figi) {
        logger.info("Получен запрос на поиск инструмента по FIGI только в кэше: {}", figi);

        try {
            List<Object> instruments = cacheInstrumentsService.getInstrumentByFigiFromCacheOnly(figi);
            logger.info("Найдено {} записей в кэше для FIGI: {}", instruments.size(), figi);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("status", "success");
            response.put("message", "Инструмент найден в кэше по FIGI: " + figi);
            response.put("figi", figi);
            response.put("total_found", instruments.size());
            response.put("instruments", instruments);
            response.put("cache_only", true);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            logger.error("Ошибка при поиске инструмента по FIGI в кэше {}: {}", figi, e.getMessage(), e);

            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("status", "error");
            errorResponse.put("message", "Ошибка при поиске инструмента по FIGI в кэше: " + e.getMessage());
            errorResponse.put("figi", figi);
            errorResponse.put("error", e.getClass().getSimpleName());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorResponse);
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

}
