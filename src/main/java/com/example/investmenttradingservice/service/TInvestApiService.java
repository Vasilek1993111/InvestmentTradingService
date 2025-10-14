package com.example.investmenttradingservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.entity.OrderEntity;
import com.example.investmenttradingservice.enums.OrderDirection;
import com.example.investmenttradingservice.enums.OrderType;

import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.contract.v1.GetOrderBookResponse;

/**
 * TInvestApiService — обертка над официальным Java SDK T-Invest для отправки
 * заявок.
 *
 * <p>
 * Использует {@link OrdersService} и метод PostOrder согласно документации
 * {@code OrdersService/PostOrder}. Реализована минимальная логика маппинга
 * полей
 * из {@link OrderEntity} в {@link PostOrderRequest}, а также базовая
 * retry-механика
 * с экспоненциальной задержкой при временных ошибках API.
 * </p>
 *
 * <p>
 * См. описание метода:
 * https://developer.tbank.ru/invest/api/orders-service-post-order
 * </p>
 */
@Service
public class TInvestApiService {

    private static final Logger logger = LoggerFactory.getLogger(TInvestApiService.class);
    private static final Logger apiLogger = LoggerFactory.getLogger("com.example.investmenttradingservice.tinvest.api");

    private OrdersService ordersService;

    private MarketDataService marketDataService;

    public TInvestApiService(OrdersService ordersService, MarketDataService marketDataService) {
        this.ordersService = ordersService;
        this.marketDataService = marketDataService;
    }

    @Value("${tinvest.api.token}")
    private String apiToken;

    // accountId используется из заявки как есть (без подстановок)

    @Value("${tinvest.api.timeout:30000}")
    private int timeoutMs;

    @Value("${tinvest.api.postorder.max-attempts:3}")
    private int maxAttempts;

    // Инициализация происходит через конфигурацию InvestApi/OrdersService

    /**
     * Отправляет заявку в T-Invest API посредством PostOrder.
     *
     * <p>
     * Поля маппятся согласно документации API. Для рыночных заявок цена передается,
     * но сервер может её игнорировать. Включена простая retry-механика
     * (экспоненциальная
     * задержка) при временных сбоях.
     * </p>
     *
     * @param order доменная заявка
     * @return результат отправки с признаком успеха и ID заявки в T-Invest
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

        int attempt = 0;
        long backoffMs = 250L; // стартовая задержка

        while (true) {
            attempt++;
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
                // Базовая эвристика: ретраим ограниченное число раз
                if (attempt >= maxAttempts) {
                    logger.error("PostOrder ошибка (попытка {} из {}): {}", attempt, maxAttempts, ex.getMessage());
                    apiLogger.error("PostOrder failed: orderId={}, instrumentId={}, attempt={}/{}, error={}, token={}",
                            mask(orderId), instrumentId, attempt, maxAttempts, safeErrorMessage(ex), apiToken);
                    return TInvestApiResponse.error(safeErrorMessage(ex));
                }

                logger.warn("PostOrder временная ошибка, retry через {} ms (attempt {}/{}): {}",
                        backoffMs, attempt, maxAttempts, ex.getMessage());
                apiLogger.warn("PostOrder retry: orderId={}, instrumentId={}, nextDelayMs={}, attempt={}/{}, token={}",
                        mask(orderId), instrumentId, backoffMs, attempt, maxAttempts, apiToken);
                sleepQuietly(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 5_000L);
            }
        }
    }

    /**
     * Отправляет заявку и возвращает сырой ответ PostOrderResponse.
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

    private static String safeErrorMessage(Exception ex) {
        String msg = ex.getMessage();
        return msg == null ? "API error" : (msg.length() > 500 ? msg.substring(0, 500) : msg);
    }

    private static void sleepQuietly(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Quotation toQuotation(BigDecimal price) {
        if (price == null) {
            // Нулевая цена для совместимости (рынок может игнорировать)
            return Quotation.newBuilder().setUnits(0).setNano(0).build();
        }
        BigDecimal scaled = price.setScale(9, RoundingMode.HALF_UP);
        long units = scaled.longValue();
        BigDecimal nanosPart = scaled.subtract(BigDecimal.valueOf(units)).movePointRight(9);
        int nanos = nanosPart.intValue();
        return Quotation.newBuilder().setUnits(units).setNano(nanos).build();
    }

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

    public List<BigDecimal> getLimitsForInstrument(String instrumentId) {
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

                List<BigDecimal> limits = new LinkedList<>();
                limits.add(limitDownDecimal);
                limits.add(limitUpDecimal);
                return limits;
            } else {
                logger.warn("Лимиты не найдены в OrderBook для инструмента {}: hasLimitUp={}, hasLimitDown={}",
                        instrumentId, limitsResponse.hasLimitUp(), limitsResponse.hasLimitDown());
            }

        } catch (Exception ex) {
            logger.error("Ошибка при получении лимитов для инструмента {}: {}", instrumentId, ex.getMessage(), ex);

        }
        logger.warn("Возвращаем пустой список лимитов для инструмента {}", instrumentId);
        return Collections.emptyList();

    }

    private BigDecimal toBigDecimal(Quotation quotation) {
        return BigDecimal.valueOf(quotation.getUnits())
                .add(BigDecimal.valueOf(quotation.getNano()).divide(BigDecimal.valueOf(10).pow(9)));
    }

}
