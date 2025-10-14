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
 * - Автоматическую отправку заявок из кэша в указанное время (start_time)
 * - Перенос просроченных заявок на следующий день
 * - Логирование событий отправки в БД для аудита
 * - Обработку ошибок при отправке
 * 
 * Работает исключительно с кэшем заявок. БД используется только для логирования
 * событий.
 * 
 * @author Investment Trading Service
 * @version 2.0
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
     * Планировщик, который запускается каждую секунду для проверки заявок из кэша.
     * 
     * Логика работы:
     * 1. Переносит просроченные заявки на следующий день
     * 2. Отправляет заявки с точным временем в T-Invest API
     * 3. Логирует события отправки в БД для аудита
     * 4. Удаляет отправленные заявки из кэша
     * 
     */

    @Scheduled(cron = "1 * * * * *")
    public void processScheduledOrders() {
        try {
            // Используем московскую таймзону и нормализуем до секунд
            LocalTime currentTime = LocalTime
                    .now(com.example.investmenttradingservice.util.TimeZoneUtils.getMoscowZone())
                    .withSecond(0).withNano(0);
            logger.debug("Проверка заявок для отправки в время: {}", currentTime);

            // Сначала обрабатываем просроченные заявки - переносим их на следующий день
            int rescheduledCount = orderCacheService.rescheduleOverdueOrders(currentTime);
            if (rescheduledCount > 0) {
                logger.info("Перенесено {} просроченных заявок на следующий день", rescheduledCount);
            }

            // Получаем заявки с точным временем (не просроченные) из кэша
            List<OrderEntity> exactTimeOrders = orderCacheService.getExactTimeOrders(currentTime);

            if (exactTimeOrders.isEmpty()) {
                logger.debug("Нет заявок для отправки в точное время: {}", currentTime);
                return;
            }

            logger.info("Найдено {} заявок для отправки в точное время: {}", exactTimeOrders.size(), currentTime);

            // Отправляем заявки с точным временем из кэша
            for (OrderEntity order : exactTimeOrders) {
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
     * Обрабатывает заявку из кэша: отправляет в T-Invest API, логирует событие в
     * БД, удаляет из кэша.
     */
    private void processSingleOrderFromCache(OrderEntity order) {
        try {
            logger.info("[CACHE] Отправка заявки ID: {}, инструмент: {}, направление: {}, количество: {}, цена: {}",
                    order.getOrderId(), order.getInstrumentId(), order.getDirection(), order.getQuantity(),
                    formatPrice(order.getPrice()));

            TInvestApiService.TInvestApiResponse response = tInvestApiService.sendOrder(order);

            if (response.isSuccess()) {
                logger.info("[CACHE] Заявка ID {} успешно отправлена, T-Invest Order ID: {}",
                        order.getOrderId(), response.getOrderId());

                // Логируем событие успешной отправки в БД (только для аудита)
                orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.SENT, response.getOrderId(),
                        null);

                // Удаляем из кэша после успешной отправки
                orderCacheService.remove(order.getOrderId());

            } else {
                logger.error("[CACHE] Ошибка отправки заявки ID {}: {}", order.getOrderId(),
                        response.getErrorMessage());

                // Логируем событие ошибки в БД (только для аудита)
                orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.ERROR, null,
                        response.getErrorMessage());

                // Удаляем из кэша даже при ошибке (заявка не будет повторно отправлена)
                orderCacheService.remove(order.getOrderId());
            }

        } catch (Exception e) {
            logger.error("[CACHE] Ошибка при отправке заявки ID {}: {}", order.getOrderId(), e.getMessage(), e);

            // Логируем событие исключения в БД (только для аудита)
            orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.ERROR, null,
                    "Ошибка отправки: " + e.getMessage());

            // Удаляем из кэша при исключении
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
     * Принудительная отправка заявки по её ID из кэша.
     * Используется для ручной отправки или повторной отправки.
     * 
     * @param orderId ID заявки для отправки
     * @return true если заявка найдена в кэше и обработана, false иначе
     */
    public boolean sendOrderById(String orderId) {
        try {
            // Ищем заявку в кэше
            List<OrderEntity> allOrders = orderCacheService.getAllEntities();
            OrderEntity order = allOrders.stream()
                    .filter(o -> orderId.equals(o.getOrderId()))
                    .findFirst()
                    .orElse(null);

            if (order == null) {
                logger.warn("Заявка с ID {} не найдена в кэше", orderId);
                return false;
            }

            logger.info("Принудительная отправка заявки ID: {} из кэша", orderId);
            processSingleOrderFromCache(order);
            return true;

        } catch (Exception e) {
            logger.error("Ошибка при принудительной отправке заявки ID {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Принудительная отправка заявки из кэша и возврат сырого ответа T-API.
     */
    public ru.tinkoff.piapi.contract.v1.PostOrderResponse forceSendOrderRaw(String orderId) {
        try {
            // Ищем заявку в кэше
            List<OrderEntity> allOrders = orderCacheService.getAllEntities();
            OrderEntity order = allOrders.stream()
                    .filter(o -> orderId.equals(o.getOrderId()))
                    .findFirst()
                    .orElse(null);

            if (order == null) {
                throw new BusinessLogicException(
                        String.format("Заявка с ID '%s' не найдена в кэше", orderId),
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

            // Логируем событие успешной отправки в БД (только для аудита)
            orderPersistenceService.updateOrderStatus(order.getOrderId(), OrderStatus.SENT, resp.getOrderId(), null);

            // Удаляем из кэша после успешной отправки
            orderCacheService.remove(order.getOrderId());

            return resp;

        } catch (Exception e) {
            logger.error("Ошибка при принудительной отправке (raw) {}: {}", orderId, e.getMessage(), e);

            // Логируем событие ошибки в БД (только для аудита)
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
     * Получает статистику по заявкам в кэше.
     * 
     * @return строка со статистикой
     */
    public String getSchedulerStatistics() {
        try {
            List<OrderEntity> allOrders = orderCacheService.getAllEntities();

            if (allOrders.isEmpty()) {
                return "Кэш заявок пуст";
            }

            StringBuilder stats = new StringBuilder();
            stats.append("Статистика кэша заявок:\n");
            stats.append("Всего заявок в кэше: ").append(allOrders.size()).append("\n");

            // Группируем по времени
            var ordersByTime = allOrders.stream()
                    .collect(java.util.stream.Collectors.groupingBy(OrderEntity::getScheduledTime));

            stats.append("Заявки по времени:\n");
            ordersByTime.entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        stats.append("  ").append(entry.getKey())
                                .append(": ").append(entry.getValue().size())
                                .append(" заявок\n");
                    });

            return stats.toString();

        } catch (Exception e) {
            logger.error("Ошибка при получении статистики планировщика: {}", e.getMessage(), e);
            return "Ошибка получения статистики";
        }
    }
}
