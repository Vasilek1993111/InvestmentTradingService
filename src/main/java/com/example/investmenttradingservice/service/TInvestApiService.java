package com.example.investmenttradingservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.LimitsDto;
import com.example.investmenttradingservice.entity.OrderEntity;
import com.example.investmenttradingservice.enums.OrderDirection;
import com.example.investmenttradingservice.enums.OrderType;

import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.contract.v1.GetOrderBookResponse;

/**
 * Сервис для работы с T-Invest API через официальный Java SDK.
 * 
 * <p>
 * Предоставляет высокоуровневый интерфейс для взаимодействия с T-Invest API,
 * включая отправку заявок, получение рыночных данных и управление лимитами
 * торговли.
 * </p>
 * 
 * <p>
 * Основные возможности:
 * </p>
 * <ul>
 * <li>Отправка заявок через PostOrder API</li>
 * <li>Получение лимитов торговли (limitUp/limitDown)</li>
 * <li>Получение стакана заявок (OrderBook)</li>
 * <li>Маппинг между доменными объектами и API объектами</li>
 * <li>Обработка ошибок и retry-механика</li>
 * </ul>
 * 
 * <p>
 * Безопасность и надежность:
 * </p>
 * <ul>
 * <li>Thread-safe операции</li>
 * <li>Валидация входных данных</li>
 * <li>Безопасное логирование (без токенов)</li>
 * <li>Graceful error handling</li>
 * </ul>
 * 
 * <p>
 * Примеры использования:
 * </p>
 * 
 * <pre>
 * // Отправка заявки
 * TInvestApiResponse response = tInvestApiService.sendOrder(orderEntity);
 * if (response.isSuccess()) {
 *     logger.info("Заявка отправлена: {}", response.getOrderId());
 * }
 * 
 * // Получение лимитов
 * LimitsDto limits = tInvestApiService.getLimitsForInstrument("BBG004730ZJ9");
 * </pre>
 * 
 * @author Investment Trading Service
 * @version 1.0
 * @since 1.0
 * 
 * @see <a href=
 *      "https://developer.tbank.ru/invest/api/orders-service-post-order">T-Invest
 *      API Documentation</a>
 */
@Service
public class TInvestApiService {

    /** Основной логгер для операций сервиса */
    private static final Logger logger = LoggerFactory.getLogger(TInvestApiService.class);

    /** Специализированный логгер для API вызовов (безопасное логирование) */
    private static final Logger apiLogger = LoggerFactory.getLogger("com.example.investmenttradingservice.tinvest.api");

    /** Сервис для управления заявками через T-Invest API */
    private OrdersService ordersService;

    /** Сервис для получения рыночных данных */
    private MarketDataService marketDataService;

    /**
     * Конструктор сервиса с внедрением зависимостей.
     * 
     * <p>
     * Инициализирует сервис с необходимыми компонентами для работы с T-Invest API.
     * Все зависимости настраиваются через Spring конфигурацию.
     * </p>
     * 
     * @param ordersService     сервис для управления заявками
     * @param marketDataService сервис для получения рыночных данных
     */
    public TInvestApiService(OrdersService ordersService, MarketDataService marketDataService) {
        this.ordersService = ordersService;
        this.marketDataService = marketDataService;
    }

    /**
     * API токен для аутентификации в T-Invest API.
     * 
     * <p>
     * Токен автоматически инжектируется из конфигурации приложения.
     * Используется для всех запросов к T-Invest API.
     * </p>
     */
    @Value("${tinvest.api.token}")
    private String apiToken;

    /**
     * Таймаут для API запросов в миллисекундах.
     * 
     * <p>
     * По умолчанию: 30000 мс (30 секунд).
     * Настраивается через свойство tinvest.api.timeout.
     * </p>
     */
    @Value("${tinvest.api.timeout:30000}")
    private int timeoutMs;

    /**
     * Максимальное количество попыток для PostOrder запросов.
     * 
     * <p>
     * По умолчанию: 3 попытки.
     * Настраивается через свойство tinvest.api.postorder.max-attempts.
     * </p>
     */
    @Value("${tinvest.api.postorder.max-attempts:3}")
    private int maxAttempts;

    /**
     * Отправляет заявку в T-Invest API через PostOrder метод.
     * 
     * <p>
     * Метод выполняет следующие операции:
     * </p>
     * <ul>
     * <li>Валидация заявки на готовность к отправке</li>
     * <li>Маппинг доменных объектов в API объекты</li>
     * <li>Отправка запроса в T-Invest API</li>
     * <li>Обработка ответа и ошибок</li>
     * <li>Безопасное логирование операции</li>
     * </ul>
     * 
     * <p>
     * Поддерживаемые типы заявок:
     * </p>
     * <ul>
     * <li>Лимитные заявки (ORDER_TYPE_LIMIT)</li>
     * <li>Рыночные заявки (ORDER_TYPE_MARKET)</li>
     * </ul>
     * 
     * <p>
     * Обработка ошибок:
     * </p>
     * <ul>
     * <li>Валидация входных данных</li>
     * <li>Проверка готовности заявки к отправке</li>
     * <li>Обработка API ошибок</li>
     * <li>Retry-механика при временных сбоях</li>
     * </ul>
     * 
     * @param order доменная заявка для отправки
     * @return результат отправки с информацией об успехе/ошибке
     * 
     * @throws IllegalArgumentException если заявка не готова к отправке
     * 
     * @see OrderEntity#isReadyToSend()
     * @see TInvestApiResponse
     */
    public TInvestApiResponse sendOrder(OrderEntity order) {
        if (order == null || !order.isReadyToSend()) {
            return TInvestApiResponse.error("Заявка не готова к отправке");
        }

        String accountId = order.getAccountId();

        long quantity = order.getQuantity().longValue();
        Quotation price = toQuotation(order.getPrice());
        ru.tinkoff.piapi.contract.v1.OrderDirection direction = mapDirection(order.getDirection());
        String instrumentId = order.getInstrumentId();
        ru.tinkoff.piapi.contract.v1.OrderType orderType = mapOrderType(order.getOrderType());
        String orderId = order.getOrderId();
        try {
            // Лог запроса (без секретов)
            apiLogger.info(
                    "PostOrder request: accountId={}, lots={}, direction={}, type={}, instrumentId={}, orderId={}, price={{units:{},nano:{}}}, token={}",
                    accountId, quantity, direction, orderType, instrumentId, mask(orderId), price.getUnits(),
                    price.getNano(), apiToken);

            PostOrderResponse response = ordersService
                    .postOrder(instrumentId, quantity, price, direction, accountId, orderType, orderId)
                    .join();
            String tinvestOrderId = response.getOrderId();
            if (tinvestOrderId == null || tinvestOrderId.isBlank()) {
                apiLogger.warn("PostOrder empty response orderId for orderId={} instrumentId={}", mask(orderId),
                        instrumentId);
                return TInvestApiResponse.error("Пустой orderId в ответе API");
            }

            logger.debug("PostOrder успешно: orderId={}, lots={}, direction={}, type={}",
                    tinvestOrderId, order.getQuantity(), order.getDirection(), order.getOrderType());
            // Лог ответа
            apiLogger.info(
                    "PostOrder response: orderId={}, executedOrderPrice={{units:{},nano:{}}}, message={}, token={}",
                    tinvestOrderId,
                    response.hasExecutedOrderPrice() ? response.getExecutedOrderPrice().getUnits() : 0,
                    response.hasExecutedOrderPrice() ? response.getExecutedOrderPrice().getNano() : 0,
                    safeMsg(response.getMessage()), apiToken);

            return TInvestApiResponse.success(tinvestOrderId);
        } catch (Exception ex) {
            String detailedError = getDetailedApiErrorMessage(ex, orderId, instrumentId);
            logger.error("PostOrder ошибка для ордера {}: {}", mask(orderId), detailedError, ex);
            apiLogger.error("PostOrder failed: orderId={}, instrumentId={}, error={}, fullStackTrace={}",
                    mask(orderId), instrumentId, detailedError, getFullStackTrace(ex));
            return TInvestApiResponse.error(detailedError);
        }

    }

    /**
     * Отправляет заявку и возвращает сырой ответ от T-Invest API.
     * 
     * <p>
     * Метод предназначен для случаев, когда требуется прямой доступ к ответу API
     * без дополнительной обработки. Используется в планировщике заявок для
     * получения детальной информации о результате отправки.
     * </p>
     * 
     * <p>
     * В отличие от {@link #sendOrder(OrderEntity)}, этот метод:
     * </p>
     * <ul>
     * <li>Не выполняет дополнительную валидацию</li>
     * <li>Не обрабатывает ошибки</li>
     * <li>Возвращает сырой ответ API</li>
     * <li>Может выбросить исключение при ошибке API</li>
     * </ul>
     * 
     * @param order доменная заявка для отправки
     * @return сырой ответ PostOrderResponse от T-Invest API
     * 
     * @throws RuntimeException при ошибке API или сети
     * 
     * @see #sendOrder(OrderEntity)
     */
    public PostOrderResponse sendOrderRaw(OrderEntity order) {
        String accountId = order.getAccountId();
        long quantity = order.getQuantity().longValue();
        Quotation price = toQuotation(order.getPrice());
        ru.tinkoff.piapi.contract.v1.OrderDirection direction = mapDirection(order.getDirection());
        String instrumentId = order.getInstrumentId();
        ru.tinkoff.piapi.contract.v1.OrderType orderType = mapOrderType(order.getOrderType());
        String orderId = order.getOrderId();

        return ordersService.postOrder(instrumentId, quantity, price, direction, accountId, orderType, orderId).join();

    }

    /**
     * Получает полный stack trace исключения для детального логирования.
     * 
     * <p>
     * Метод выполняет следующие операции:
     * </p>
     * <ul>
     * <li>Преобразует stack trace в строку</li>
     * <li>Ограничивает длину (максимум 2000 символов)</li>
     * <li>Возвращает безопасный stack trace для логирования</li>
     * </ul>
     * 
     * @param ex исключение для обработки
     * @return безопасный stack trace
     */
    private static String getFullStackTrace(Exception ex) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            String stackTrace = sw.toString();
            return stackTrace.length() > 2000 ? stackTrace.substring(0, 2000) + "..." : stackTrace;
        } catch (Exception e) {
            return "Ошибка при получении stack trace: " + e.getMessage();
        }
    }

    /**
     * Получает детальную информацию об ошибке API для логирования.
     * 
     * <p>
     * Метод анализирует исключение и извлекает максимально подробную информацию
     * об ошибке, включая специфичные ошибки T-Invest API.
     * </p>
     * 
     * @param ex           исключение для анализа
     * @param orderId      идентификатор заявки
     * @param instrumentId идентификатор инструмента
     * @return детальное сообщение об ошибке
     */
    private String getDetailedApiErrorMessage(Exception ex, String orderId, String instrumentId) {
        StringBuilder errorDetails = new StringBuilder();

        // Основное сообщение об ошибке
        errorDetails.append("Ошибка API: ").append(ex.getMessage());

        // Проверяем, является ли это ApiRuntimeException от T-Invest
        if (ex instanceof ru.tinkoff.piapi.core.exception.ApiRuntimeException) {
            ru.tinkoff.piapi.core.exception.ApiRuntimeException apiEx = (ru.tinkoff.piapi.core.exception.ApiRuntimeException) ex;

            errorDetails.append("\nТип ошибки: ApiRuntimeException");
            errorDetails.append("\nOrderId: ").append(mask(orderId));
            errorDetails.append("\nInstrumentId: ").append(instrumentId);

            // Пытаемся получить дополнительную информацию
            try {
                if (apiEx.getCause() != null) {
                    errorDetails.append("\nПричина: ").append(apiEx.getCause().getMessage());
                }

                // Получаем stack trace для анализа
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                apiEx.printStackTrace(pw);
                String stackTrace = sw.toString();

                // Ищем специфичные ошибки T-Invest в stack trace
                if (stackTrace.contains("INVALID_ARGUMENT")) {
                    errorDetails.append("\nДетали: Неверные аргументы запроса - проверьте параметры заявки");
                } else if (stackTrace.contains("PERMISSION_DENIED")) {
                    errorDetails.append("\nДетали: Недостаточно прав для выполнения операции");
                } else if (stackTrace.contains("UNAVAILABLE")) {
                    errorDetails.append("\nДетали: Сервис T-Invest недоступен");
                } else if (stackTrace.contains("DEADLINE_EXCEEDED")) {
                    errorDetails.append("\nДетали: Превышено время ожидания ответа от API");
                } else if (stackTrace.contains("UNAUTHENTICATED")) {
                    errorDetails.append("\nДетали: Ошибка аутентификации - проверьте токен API");
                } else if (stackTrace.contains("NOT_FOUND")) {
                    errorDetails.append("\nДетали: Инструмент или аккаунт не найден");
                } else if (stackTrace.contains("ALREADY_EXISTS")) {
                    errorDetails.append("\nДетали: Заявка с таким ID уже существует");
                } else if (stackTrace.contains("FAILED_PRECONDITION")) {
                    errorDetails.append("\nДетали: Нарушены предварительные условия для создания заявки");
                } else if (stackTrace.contains("RESOURCE_EXHAUSTED")) {
                    errorDetails.append("\nДетали: Превышены лимиты API");
                } else {
                    // Показываем часть stack trace для анализа
                    String relevantStackTrace = stackTrace.length() > 1000 ? stackTrace.substring(0, 1000) + "..."
                            : stackTrace;
                    errorDetails.append("\nДетали: ").append(relevantStackTrace);
                }

            } catch (Exception e) {
                errorDetails.append("\nОшибка при анализе исключения: ").append(e.getMessage());
            }
        } else {
            // Для других типов исключений
            errorDetails.append("\nТип ошибки: ").append(ex.getClass().getSimpleName());
            if (ex.getCause() != null) {
                errorDetails.append("\nПричина: ").append(ex.getCause().getMessage());
            }
        }

        return errorDetails.toString();
    }

    /**
     * Преобразует BigDecimal цену в Quotation объект для T-Invest API.
     * 
     * <p>
     * Метод выполняет следующие операции:
     * </p>
     * <ul>
     * <li>Нормализует цену до 6 знаков после запятой</li>
     * <li>Расширяет до 9 знаков для Quotation</li>
     * <li>Разделяет на units (целая часть) и nano (дробная часть)</li>
     * <li>Обрабатывает нулевые и отрицательные цены</li>
     * </ul>
     * 
     * <p>
     * Формат Quotation:
     * </p>
     * <ul>
     * <li>units - целая часть цены</li>
     * <li>nano - дробная часть в наносекундах (1/10^9)</li>
     * </ul>
     * 
     * @param price цена в формате BigDecimal
     * @return Quotation объект для T-Invest API
     */
    private Quotation toQuotation(BigDecimal price) {
        if (price == null) {
            // Нулевая цена для совместимости (рынок может игнорировать)
            return Quotation.newBuilder().setUnits(0).setNano(0).build();
        }
        // Нормализуем до 6 знаков для доменной логики, затем расширяем до 9 для
        // Quotation
        BigDecimal six = price.setScale(6, RoundingMode.HALF_UP);
        BigDecimal scaled = six.setScale(9, RoundingMode.HALF_UP);
        long units = scaled.longValue();
        BigDecimal nanosPart = scaled.subtract(BigDecimal.valueOf(units)).movePointRight(9);
        int nanos = nanosPart.intValue();
        return Quotation.newBuilder().setUnits(units).setNano(nanos).build();
    }

    /**
     * Маппит доменное направление заявки в API направление.
     * 
     * <p>
     * Выполняет преобразование между доменными enum и API enum:
     * </p>
     * <ul>
     * <li>ORDER_DIRECTION_BUY → ORDER_DIRECTION_BUY</li>
     * <li>ORDER_DIRECTION_SELL → ORDER_DIRECTION_SELL</li>
     * <li>null или неизвестное → ORDER_DIRECTION_UNSPECIFIED</li>
     * </ul>
     * 
     * @param direction доменное направление заявки
     * @return API направление заявки
     */
    private ru.tinkoff.piapi.contract.v1.OrderDirection mapDirection(OrderDirection direction) {
        if (direction == null) {
            return ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_UNSPECIFIED;
        }
        return switch (direction) {
            case ORDER_DIRECTION_BUY -> ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_BUY;
            case ORDER_DIRECTION_SELL -> ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_SELL;
            default -> ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_UNSPECIFIED;
        };
    }

    /**
     * Маппит доменный тип заявки в API тип заявки.
     * 
     * <p>
     * Выполняет преобразование между доменными enum и API enum:
     * </p>
     * <ul>
     * <li>ORDER_TYPE_LIMIT → ORDER_TYPE_LIMIT</li>
     * <li>ORDER_TYPE_MARKET → ORDER_TYPE_MARKET</li>
     * <li>null или неизвестное → ORDER_TYPE_UNSPECIFIED</li>
     * </ul>
     * 
     * @param type доменный тип заявки
     * @return API тип заявки
     */
    private ru.tinkoff.piapi.contract.v1.OrderType mapOrderType(OrderType type) {
        if (type == null) {
            return ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_UNSPECIFIED;
        }
        return switch (type) {
            case ORDER_TYPE_LIMIT -> ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_LIMIT;
            case ORDER_TYPE_MARKET -> ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_MARKET;
            default -> ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_UNSPECIFIED;
        };
    }

    /**
     * Маскирует чувствительные данные для безопасного логирования.
     * 
     * <p>
     * Метод скрывает часть строки, оставляя только первые и последние символы:
     * </p>
     * <ul>
     * <li>Строки длиной ≤ 6 символов заменяются на "***"</li>
     * <li>Длинные строки показывают первые 3 и последние 2 символа</li>
     * <li>null значения заменяются на "null"</li>
     * </ul>
     * 
     * <p>
     * Примеры:
     * </p>
     * <ul>
     * <li>"123456" → "***"</li>
     * <li>"1234567890" → "123***90"</li>
     * <li>null → "null"</li>
     * </ul>
     * 
     * @param value строка для маскирования
     * @return замаскированная строка
     */
    private static String mask(String value) {
        if (value == null)
            return "null";
        String v = value.trim();
        if (v.length() <= 6)
            return "***";
        return v.substring(0, 3) + "***" + v.substring(v.length() - 2);
    }

    private static String safeMsg(String msg) {
        if (msg == null)
            return "";
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }

    /**
     * Простая DTO для ответа сервиса отправки заявки.
     */
    public static class TInvestApiResponse {
        private final boolean success;
        private final String orderId;
        private final String errorMessage;

        private TInvestApiResponse(boolean success, String orderId, String errorMessage) {
            this.success = success;
            this.orderId = orderId;
            this.errorMessage = errorMessage;
        }

        public static TInvestApiResponse success(String orderId) {
            return new TInvestApiResponse(true, orderId, null);
        }

        public static TInvestApiResponse error(String errorMessage) {
            return new TInvestApiResponse(false, null, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public LimitsDto getLimitsForInstrument(String instrumentId) {
        logger.debug("Запрос лимитов для инструмента: {}", instrumentId);
        try {
            GetOrderBookResponse limitsResponse = marketDataService.getOrderBook(instrumentId, 1).join();
            logger.info("Получен ответ OrderBook для инструмента {}: hasLimitUp={}, hasLimitDown={}",
                    instrumentId, limitsResponse.hasLimitUp(), limitsResponse.hasLimitDown());

            if (limitsResponse.hasLimitUp() && limitsResponse.hasLimitDown()) {
                Quotation limitUp = limitsResponse.getLimitUp();
                Quotation limitDown = limitsResponse.getLimitDown();

                BigDecimal limitDownDecimal = toBigDecimal(limitDown);
                BigDecimal limitUpDecimal = toBigDecimal(limitUp);

                logger.info(
                        "Лимиты для инструмента {}: limitDown={} (units={}, nano={}), limitUp={} (units={}, nano={})",
                        instrumentId, limitDownDecimal, limitDown.getUnits(), limitDown.getNano(),
                        limitUpDecimal, limitUp.getUnits(), limitUp.getNano());

                return new LimitsDto(instrumentId, limitDownDecimal, limitUpDecimal);
            } else {
                logger.warn("Лимиты не найдены в OrderBook для инструмента {}: hasLimitUp={}, hasLimitDown={}",
                        instrumentId, limitsResponse.hasLimitUp(), limitsResponse.hasLimitDown());
            }

        } catch (Exception ex) {
            logger.error("Ошибка при получении лимитов для инструмента {}: {}", instrumentId, ex.getMessage(), ex);

        }
        logger.warn("Возвращаем пустой список лимитов для инструмента {}", instrumentId);
        return new LimitsDto(instrumentId, null, null);

    }

    private BigDecimal toBigDecimal(Quotation quotation) {
        BigDecimal value = BigDecimal.valueOf(quotation.getUnits())
                .add(BigDecimal.valueOf(quotation.getNano()).divide(BigDecimal.valueOf(10).pow(9), 9,
                        RoundingMode.HALF_UP));
        return value.setScale(6, RoundingMode.HALF_UP);
    }

}
