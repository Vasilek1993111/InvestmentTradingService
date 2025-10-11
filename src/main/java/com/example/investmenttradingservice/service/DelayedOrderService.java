package com.example.investmenttradingservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.GroupOrderRequest;
import com.example.investmenttradingservice.DTO.GroupOrderResponseDTO;
import com.example.investmenttradingservice.DTO.OrderDTO;
import java.math.BigDecimal;

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
            // Валидируем запрос
            if (!isValidRequest(request)) {
                logger.error("Некорректный запрос групповой заявки");
                return List.of();
            }

            // Генерируем заявки
            List<OrderDTO> orders = orderGenerationService.generateOrders(request);

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

    /**
     * Обрабатывает групповую заявку и возвращает ответ с заявками и ценой
     * инструмента.
     * 
     * @param request запрос на создание групповой заявки
     * @return ответ с заявками и реальной ценой инструмента из API
     */
    public GroupOrderResponseDTO processGroupOrderWithPrice(GroupOrderRequest request) {
        logger.info("Начало обработки групповой заявки с ценой: {} инструментов, направление: {}, уровни: {}",
                request.instruments().size(), request.direction(), request.levels().getLevelsCount());

        try {
            // Валидируем запрос
            if (!isValidRequest(request)) {
                logger.error("Некорректный запрос групповой заявки");
                return GroupOrderResponseDTO.empty();
            }

            // Генерируем заявки и получаем реальную цену инструмента из API
            var result = orderGenerationService.generateOrdersWithInstrumentPrice(request);
            List<OrderDTO> orders = result.getKey();
            BigDecimal instrumentPrice = result.getValue();

            logger.info("Получена реальная цена инструмента из API: {}", instrumentPrice);

            // Сначала кладем в кэш, затем дублируем в БД
            if (!orders.isEmpty()) {
                orderCacheService.putAll(orders);
                logger.info("Заявки сохранены в кэш");
                orderPersistenceService.saveOrders(orders);
                logger.info("Заявки сохранены в БД (events store)");
            }

            // Логируем результат
            logGenerationResult(request, orders);

            return GroupOrderResponseDTO.of(orders, instrumentPrice);

        } catch (Exception e) {
            logger.error("Ошибка при обработке групповой заявки: {}", e.getMessage(), e);
            return GroupOrderResponseDTO.empty();
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