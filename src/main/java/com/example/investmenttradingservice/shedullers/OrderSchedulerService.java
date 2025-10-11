package com.example.investmenttradingservice.shedullers;

import com.example.investmenttradingservice.entity.OrderEntity;
import com.example.investmenttradingservice.enums.OrderStatus;
import com.example.investmenttradingservice.service.OrderPersistenceService;
import com.example.investmenttradingservice.service.TInvestApiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import com.example.investmenttradingservice.exception.BusinessLogicException;

/**
 * Сервис для планирования и отправки заявок по расписанию.
 * 
 * Отвечает за:
 * - Автоматическую отправку заявок в указанное время (start_time)
 * - Мониторинг статуса заявок
 * - Обработку ошибок при отправке
 * 
 * @author Investment Trading Service
 * @version 1.0
 */
@Service
public class OrderSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(OrderSchedulerService.class);
    private final int DELAY_MS = 100;
    @Autowired
    private OrderPersistenceService orderPersistenceService;

    @Autowired
    private TInvestApiService tInvestApiService;

    @Autowired
    @Qualifier("criticalOperationsExecutor")
    private Executor criticalOperationsExecutor;

    @Autowired
    private com.example.investmenttradingservice.service.OrderCacheService orderCacheService;

    /**
     * Планировщик, который запускается каждую минуту для проверки заявок,
     * готовых к отправке в текущее время.
     * 
     * Cron выражение: "0 * * * * ?" означает запуск в 0 секунд каждой минуты
     */
    @Scheduled(cron = "*/1 * * * * *")
    public void processScheduledOrders() {
        try {
            LocalTime currentTime = LocalTime.now();
            logger.debug("Проверка заявок для отправки в время: {}", currentTime);

            // Получаем все заявки, готовые к отправке в текущее время
            List<OrderEntity> ordersToSend = orderPersistenceService.findOrdersReadyToSend(currentTime);

            if (ordersToSend.isEmpty()) {
                logger.debug("Нет заявок для отправки в время: {}", currentTime);
                return;
            }

            logger.info("Найдено {} заявок для отправки в время: {}", ordersToSend.size(), currentTime);

            // Читаем заявки из кэша, а не из БД
            List<OrderEntity> cachedDue = orderCacheService.getDue(currentTime);
            if (cachedDue.isEmpty()) {
                logger.debug("В кэше нет заявок к отправке на {}", currentTime);
                return;
            }

            for (OrderEntity order : cachedDue) {
                CompletableFuture.runAsync(() -> {
                    // Задержка 100мс перед отправкой
                    try {
                        TimeUnit.MILLISECONDS.sleep(DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    processSingleOrderFromCache(order);
                }, criticalOperationsExecutor);
            }

        } catch (Exception e) {
            logger.error("Ошибка при обработке запланированных заявок: {}", e.getMessage(), e);
        }
    }

    /**
     * Обрабатывает одну заявку - отправляет её в T-Invest API.
     * 
     * @param order заявка для отправки
     */
    private void processSingleOrder(OrderEntity order) {
        try {
            logger.info("Отправка заявки ID: {}, инструмент: {}, направление: {}, количество: {}, цена: {}",
                    order.getOrderId(),
                    order.getInstrumentId(),
                    order.getDirection(),
                    order.getQuantity(),
                    formatPrice(order.getPrice()));

            // Отправка заявки через T-Invest API
            TInvestApiService.TInvestApiResponse response = tInvestApiService.sendOrder(order);

            if (response.isSuccess()) {
                logger.info("Заявка ID {} успешно отправлена в T-Invest API, Order ID: {}",
                        order.getOrderId(), response.getOrderId());
                orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.SENT,
                        response.getOrderId(), null);
            } else {
                logger.error("Ошибка отправки заявки ID {} в T-Invest API: {}",
                        order.getOrderId(), response.getErrorMessage());
                orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.ERROR,
                        null, response.getErrorMessage());
            }

        } catch (Exception e) {
            logger.error("Ошибка при отправке заявки ID {}: {}", order.getOrderId(), e.getMessage(), e);

            // Помечаем заявку как ошибочную
            orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.ERROR,
                    null, "Ошибка отправки: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает заявку из кэша: отправляет, обновляет БД, затем удаляет из кэша.
     */
    private void processSingleOrderFromCache(OrderEntity order) {
        try {
            logger.info("[CACHE] Отправка заявки ID: {}, инструмент: {}, направление: {}, количество: {}, цена: {}",
                    order.getOrderId(), order.getInstrumentId(), order.getDirection(), order.getQuantity(),
                    formatPrice(order.getPrice()));

            TInvestApiService.TInvestApiResponse response = tInvestApiService.sendOrder(order);

            if (response.isSuccess()) {
                logger.info("[CACHE] Заявка ID {} успешно отправлена, Order ID: {}", order.getOrderId(),
                        response.getOrderId());
                orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.SENT, response.getOrderId(),
                        null);
                orderCacheService.remove(order.getOrderId());
            } else {
                logger.error("[CACHE] Ошибка отправки заявки ID {}: {}", order.getOrderId(),
                        response.getErrorMessage());
                orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.ERROR, null,
                        response.getErrorMessage());
                orderCacheService.remove(order.getOrderId());
            }
        } catch (Exception e) {
            logger.error("[CACHE] Ошибка при отправке заявки ID {}: {}", order.getOrderId(), e.getMessage(), e);
            orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.ERROR, null,
                    "Ошибка отправки: " + e.getMessage());
            orderCacheService.remove(order.getOrderId());
        }
    }

    /**
     * Форматирует цену для логирования.
     * 
     * @param units целая часть цены
     * @param nano  дробная часть цены
     * @return отформатированная цена
     */
    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) {
            return "null";
        }
        return price.setScale(4, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Принудительная отправка заявки по её ID.
     * Используется для ручной отправки или повторной отправки.
     * 
     * @param orderId ID заявки для отправки
     * @return true если заявка найдена и обработана, false иначе
     */
    public boolean sendOrderById(String orderId) {
        try {
            OrderEntity order = orderPersistenceService.findOrderByOrderId(orderId).orElse(null);
            if (order == null) {
                logger.warn("Заявка с ID {} не найдена", orderId);
                return false;
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                logger.warn("Заявка ID {} уже обработана, текущий статус: {}", orderId, order.getStatus());
                return false;
            }

            logger.info("Принудительная отправка заявки ID: {}", orderId);
            processSingleOrder(order);
            return true;

        } catch (Exception e) {
            logger.error("Ошибка при принудительной отправке заявки ID {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Принудительная отправка заявки и возврат сырого ответа T-API.
     */
    public ru.tinkoff.piapi.contract.v1.PostOrderResponse forceSendOrderRaw(String orderId) {
        try {
            OrderEntity order = orderPersistenceService.findOrderByOrderId(orderId).orElse(null);
            if (order == null) {
                throw new BusinessLogicException(
                        String.format("Заявка с ID '%s' не найдена", orderId),
                        "ORDER_NOT_FOUND");
            }
            if (!order.isReadyToSend()) {
                throw new BusinessLogicException(
                        String.format(
                                "Заявка '%s' не готова к отправке (проверьте accountId, instrumentId, price, quantity)",
                                orderId),
                        "ORDER_NOT_READY");
            }
            ru.tinkoff.piapi.contract.v1.PostOrderResponse resp = tInvestApiService.sendOrderRaw(order);
            orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.SENT, resp.getOrderId(), null);
            orderCacheService.remove(order.getOrderId());
            return resp;
        } catch (Exception e) {
            logger.error("Ошибка при принудительной отправке (raw) {}: {}", orderId, e.getMessage(), e);
            // фиксация ошибки в БД
            try {
                orderPersistenceService.updateOrderStatus(orderId, OrderStatus.ERROR, null, e.getMessage());
            } catch (Exception ignore) {
            }
            throw new BusinessLogicException(
                    String.format("T-Invest API ошибка при отправке заявки '%s': %s", orderId, e.getMessage()),
                    "TINVEST_API_ERROR");
        }
    }

    /**
     * Получает статистику по запланированным заявкам.
     * 
     * @return строка со статистикой
     */
    public String getSchedulerStatistics() {
        try {
            return orderPersistenceService.getOrdersStatistics();
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики планировщика: {}", e.getMessage(), e);
            return "Ошибка получения статистики";
        }
    }
}
