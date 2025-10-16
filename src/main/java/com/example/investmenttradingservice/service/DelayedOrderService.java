package com.example.investmenttradingservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.GroupOrderRequest;
import com.example.investmenttradingservice.DTO.OrderDTO;
import com.example.investmenttradingservice.entity.OrderEntity;
import com.example.investmenttradingservice.enums.OrderStatus;
import com.example.investmenttradingservice.mapper.OrderMapper;

/**
 * Сервис для обработки отложенных заявок.
 * Обрабатывает групповые заявки и генерирует список одиночных заявок.
 * 
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Обработка групповых заявок (GroupOrderRequest)</li>
 * <li>Генерация списка одиночных заявок</li>
 * <li>Валидация и логирование процесса</li>
 * </ul>
 */
@Service
public class DelayedOrderService {

    /** Логгер для записи операций сервиса */
    private static final Logger logger = LoggerFactory.getLogger(DelayedOrderService.class);

    /** Сервис для генерации заявок */
    @Autowired
    private OrderGenerationService orderGenerationService;

    /** Сервис для работы с заявками в БД */
    @Autowired
    private OrderPersistenceService orderPersistenceService;

    /** Кэш заявок для быстрого доступа */
    @Autowired
    private OrderCacheService orderCacheService;

    /** Маппер для преобразования DTO <-> Entity */
    @Autowired
    private OrderMapper orderMapper;

    /** Сервис отправки заявок в T-Invest API */
    @Autowired
    private TInvestApiService tInvestApiService;

    /**
     * Обрабатывает групповую заявку и возвращает список сгенерированных заявок.
     * 
     * @param request запрос на создание групповой заявки
     * @return список сгенерированных заявок
     */
    public List<OrderDTO> processGroupOrder(GroupOrderRequest request) {
        logger.info("Начало обработки групповой заявки: {} инструментов, направление: {}, уровни: {}",
                request.instruments().size(), request.direction(), request.levels().getLevelsCount());

        try {
            // Валидация времени: start_time не должен быть в прошлом (в таймзоне
            // Europe/Moscow)
            java.time.LocalTime now = java.time.LocalTime
                    .now(com.example.investmenttradingservice.util.TimeZoneUtils.getMoscowZone())
                    .withSecond(0).withNano(0);
            if (request.start_time() != null && request.start_time().isBefore(now)) {
                throw new com.example.investmenttradingservice.exception.ValidationException(
                        "Время начала исполнения (start_time) не может быть в прошлом",
                        "start_time",
                        request.start_time());
            }
            // Валидируем запрос
            if (!isValidRequest(request)) {
                logger.error("Некорректный запрос групповой заявки");
                return List.of();
            }

            // Генерируем заявки
            List<OrderDTO> orders = orderGenerationService.generateOrders(request);

            // Если пользователь передал "now" -> start_time уже десериализован в текущее
            // время.
            // Такие заявки отправляем сразу в T-Invest API, не кладем в кэш и БД.
            java.time.LocalTime nowNormalized = java.time.LocalTime
                    .now(com.example.investmenttradingservice.util.TimeZoneUtils.getMoscowZone())
                    .withSecond(0).withNano(0);
            boolean isImmediate = request.start_time() != null && request.start_time().equals(nowNormalized);

            if (isImmediate && !orders.isEmpty()) {
                logger.info("Обнаружены мгновенные заявки (now): сохраняем в БД и отправляем напрямую в T-Invest API");
                for (OrderDTO dto : orders) {
                    try {
                        // 1) Сначала фиксируем заявку в БД со статусом PENDING
                        orderPersistenceService.saveOrder(dto);

                        // 2) Пробуем отправить заявку
                        OrderEntity entity = orderMapper.toEntity(dto);
                        TInvestApiService.TInvestApiResponse apiResponse = tInvestApiService.sendOrder(entity);

                        // 3) Обновляем статус в БД в зависимости от результата
                        if (apiResponse.isSuccess()) {
                            orderPersistenceService.updateOrderStatus(dto.orderId(), OrderStatus.SENT,
                                    apiResponse.getOrderId(), null);
                            logger.info("Мгновенная заявка отправлена: orderId={}, tinvestOrderId={}", dto.orderId(),
                                    apiResponse.getOrderId());
                        } else {
                            orderPersistenceService.updateOrderStatus(dto.orderId(), OrderStatus.ERROR, null,
                                    apiResponse.getErrorMessage());
                            logger.error("Мгновенная заявка не отправлена: orderId={}, error={}", dto.orderId(),
                                    apiResponse.getErrorMessage());
                        }
                    } catch (Exception ex) {
                        // На случай непредвиденной ошибки — зафиксируем ERROR в БД
                        orderPersistenceService.updateOrderStatus(dto.orderId(), OrderStatus.ERROR, null,
                                "Exception: " + ex.getMessage());
                        logger.error("Ошибка при обработке мгновенной заявки {}: {}", dto.orderId(), ex.getMessage(),
                                ex);
                    }
                }
                return orders;
            }

            // Сначала кладем в кэш, затем дублируем в БД
            if (!orders.isEmpty()) {
                orderCacheService.putAll(orders);
                logger.info("Заявки сохранены в кэш");
                orderPersistenceService.saveOrders(orders);
                logger.info("Заявки сохранены в БД (events store)");
            }

            // Логируем результат
            logGenerationResult(request, orders);

            return orders;

        } catch (Exception e) {
            logger.error("Ошибка при обработке групповой заявки: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // Удалено: processGroupOrderWithPrice - заменён на processSingleOrderWithPrice
    // для одноинструментных запросов

    /**
     * Обрабатывает одиночный запрос с финальными ценами уровней: сохраняет в БД,
     * при необходимости отправляет немедленно ("now"), выставляя статусы.
     */
    public List<OrderDTO> processSingleOrderWithPrice(
            com.example.investmenttradingservice.DTO.SingleOrderRequest request) {
        logger.info("Начало обработки одиночной заявки с ценой: инструмент={}, направления={}, уровни={}",
                request.instrument(), request.direction(), request.levels().getLevelsCount());

        try {
            // Валидация времени
            java.time.LocalTime now = java.time.LocalTime
                    .now(com.example.investmenttradingservice.util.TimeZoneUtils.getMoscowZone())
                    .withSecond(0).withNano(0);
            if (request.start_time() != null && request.start_time().isBefore(now)) {
                throw new com.example.investmenttradingservice.exception.ValidationException(
                        "Время начала исполнения (start_time) не может быть в прошлом",
                        "start_time",
                        request.start_time());
            }

            List<OrderDTO> orders = orderGenerationService.generateSingleInstrumentWithDirectLevelPrices(
                    request.instrument(), request.direction(), request.amount(), request.levels(),
                    request.start_time());

            if (orders.isEmpty()) {
                return List.of();
            }

            // Немедленная отправка
            java.time.LocalTime nowNormalized = now;
            boolean isImmediate = request.start_time() != null && request.start_time().equals(nowNormalized);
            if (isImmediate) {
                for (OrderDTO dto : orders) {
                    try {
                        orderPersistenceService.saveOrder(dto);
                        com.example.investmenttradingservice.entity.OrderEntity entity = orderMapper.toEntity(dto);
                        TInvestApiService.TInvestApiResponse apiResponse = tInvestApiService.sendOrder(entity);
                        if (apiResponse.isSuccess()) {
                            orderPersistenceService.updateOrderStatus(dto.orderId(),
                                    com.example.investmenttradingservice.enums.OrderStatus.SENT,
                                    apiResponse.getOrderId(), null);
                        } else {
                            orderPersistenceService.updateOrderStatus(dto.orderId(),
                                    com.example.investmenttradingservice.enums.OrderStatus.ERROR,
                                    null, apiResponse.getErrorMessage());
                        }
                    } catch (Exception ex) {
                        orderPersistenceService.updateOrderStatus(dto.orderId(),
                                com.example.investmenttradingservice.enums.OrderStatus.ERROR,
                                null, "Exception: " + ex.getMessage());
                    }
                }
                return orders;
            }

            // Отложенная логика: кэш + БД
            orderCacheService.putAll(orders);
            orderPersistenceService.saveOrders(orders);
            return orders;

        } catch (Exception e) {
            logger.error("Ошибка при обработке одиночной заявки с ценой: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Валидирует групповой запрос.
     * 
     * @param request запрос на валидацию
     * @return true если запрос корректен
     */
    private boolean isValidRequest(GroupOrderRequest request) {
        if (request == null) {
            logger.warn("Запрос равен null");
            return false;
        }

        if (request.instruments() == null || request.instruments().isEmpty()) {
            logger.warn("Список инструментов пуст");
            return false;
        }

        if (request.levels() == null) {
            logger.warn("Уровни не заданы");
            return false;
        }

        if (request.levels().getLevelsCount() == 0) {
            logger.warn("Количество уровней равно нулю");
            return false;
        }

        if (request.amount() == null || request.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            logger.warn("Сумма не задана или равна нулю: {}", request.amount());
            return false;
        }

        if (request.direction() == null || request.direction().trim().isEmpty()) {
            logger.warn("Направление торговли не задано");
            return false;
        }

        return true;
    }

    /**
     * Логирует результат генерации заявок.
     * 
     * @param request исходный запрос
     * @param orders  сгенерированные заявки
     */
    private void logGenerationResult(GroupOrderRequest request, List<OrderDTO> orders) {
        logger.info("Результат генерации заявок:");
        logger.info("- Исходный запрос: {} инструментов, направление: {}, сумма: {}, уровни: {}",
                request.instruments().size(), request.direction(), request.amount(), request.levels().getLevelsCount());
        logger.info("- Сгенерировано заявок: {}", orders.size());

        // Группируем заявки по направлениям для статистики
        long buyOrders = orders.stream()
                .filter(order -> order.direction().getValue().contains("BUY"))
                .count();
        long sellOrders = orders.stream()
                .filter(order -> order.direction().getValue().contains("SELL"))
                .count();

        logger.info("- Заявки на покупку: {}", buyOrders);
        logger.info("- Заявки на продажу: {}", sellOrders);

        // Логируем детали каждой заявки
        if (logger.isDebugEnabled()) {
            for (OrderDTO order : orders) {
                logger.debug("Заявка: {} - {} {} лотов по цене {}",
                        order.instrumentId(),
                        order.direction().getValue(),
                        order.quantity(),
                        order.getPriceAsBigDecimal());
            }
        }
    }

    /**
     * Получает статистику по заявкам.
     * 
     * @param orders список заявок
     * @return строка со статистикой
     */
    public String getOrdersStatistics(List<OrderDTO> orders) {
        if (orders == null || orders.isEmpty()) {
            return "Заявки не сгенерированы";
        }

        long buyOrders = orders.stream()
                .filter(order -> order.direction().getValue().contains("BUY"))
                .count();
        long sellOrders = orders.stream()
                .filter(order -> order.direction().getValue().contains("SELL"))
                .count();

        int totalQuantity = orders.stream()
                .mapToInt(OrderDTO::quantity)
                .sum();

        return String.format("Всего заявок: %d (покупка: %d, продажа: %d), общее количество лотов: %d",
                orders.size(), buyOrders, sellOrders, totalQuantity);
    }

    /**
     * Получает статистику по заявкам из базы данных.
     * 
     * @return строка со статистикой из БД
     */
    public String getOrdersStatisticsFromDB() {
        return orderPersistenceService.getOrdersStatistics();
    }

    /**
     * Находит заявки, готовые к отправке в указанное время.
     * 
     * @param scheduledTime время исполнения
     * @return список заявок, готовых к отправке
     */
    public List<OrderDTO> findOrdersReadyToSend(java.time.LocalTime scheduledTime) {
        try {
            return orderPersistenceService.findOrdersReadyToSendDTO(scheduledTime);
        } catch (Exception e) {
            logger.error("Ошибка при поиске заявок для отправки: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Находит заявки с ошибками.
     * 
     * @return список заявок с ошибками
     */
    public List<OrderDTO> findOrdersWithErrors() {
        try {
            return orderPersistenceService.findOrdersWithErrorsDTO();
        } catch (Exception e) {
            logger.error("Ошибка при поиске заявок с ошибками: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Обновляет статус заявки.
     * 
     * @param orderId      идентификатор заявки
     * @param status       новый статус
     * @param apiResponse  ответ от API
     * @param errorMessage сообщение об ошибке
     * @return true если статус обновлен
     */
    public boolean updateOrderStatus(String orderId, com.example.investmenttradingservice.enums.OrderStatus status,
            String apiResponse, String errorMessage) {
        return orderPersistenceService.updateOrderStatus(orderId, status, apiResponse, errorMessage);
    }
}