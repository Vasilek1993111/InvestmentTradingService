package com.example.investmenttradingservice.controller;

import com.example.investmenttradingservice.DTO.GroupOrderRequest;
import com.example.investmenttradingservice.DTO.GroupOrderResponseDTO;
import com.example.investmenttradingservice.DTO.OrderDTO;
import com.example.investmenttradingservice.DTO.OrderResponseDTO;
import com.example.investmenttradingservice.DTO.TinkoffPostOrderResponseDTO;
import com.example.investmenttradingservice.DTO.ApiSuccessResponse;
import com.example.investmenttradingservice.DTO.LimitOrderRequest;
import com.example.investmenttradingservice.DTO.LimitOrderResponse;
import com.example.investmenttradingservice.exception.ValidationException;
import com.example.investmenttradingservice.exception.BusinessLogicException;

import com.example.investmenttradingservice.enums.OrderStatus;
import com.example.investmenttradingservice.service.DelayedOrderService;
import com.example.investmenttradingservice.service.OrderPersistenceService;
import com.example.investmenttradingservice.shedullers.OrderSchedulerService;
import com.example.investmenttradingservice.service.OrderCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

/**
 * REST контроллер для управления заявками и планировщиком.
 * 
 * Предоставляет endpoints для:
 * - Создания групповых заявок
 * - Управления отдельными заявками
 * - Мониторинга планировщика
 * - Получения статистики
 * 
 * @author Investment Trading Service
 * @version 1.0
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private DelayedOrderService delayedOrderService;

    @Autowired
    private OrderPersistenceService orderPersistenceService;

    @Autowired
    private OrderSchedulerService orderSchedulerService;

    @Autowired
    private OrderCacheService orderCacheService;

    @Autowired
    private OrderGenerationService orderGenerationService;

    /**
     * Создает групповую заявку.
     * 
     * @param request данные групповой заявки
     * @return список созданных заявок
     */
    @PostMapping("/group")
    public ResponseEntity<List<OrderDTO>> createGroupOrder(@RequestBody GroupOrderRequest request) {
        try {
            logger.info("Создание групповой заявки для {} инструментов, время: {}",
                    request.instruments().size(), request.start_time());

            List<OrderDTO> orders = delayedOrderService.processGroupOrder(request);

            if (orders.isEmpty()) {
                logger.warn("Не удалось создать заявки для группового запроса");
                throw new BusinessLogicException(
                        "Не удалось создать заявки для группового запроса",
                        "ORDERS_NOT_CREATED");
            }

            logger.info("Создано {} заявок для группового запроса", orders.size());
            return ResponseEntity.ok(orders);

        } catch (com.example.investmenttradingservice.exception.ValidationException e) {
            logger.warn("Валидация не пройдена для групповой заявки: {}", e.getMessage());
            throw e;
        } catch (com.example.investmenttradingservice.exception.BusinessLogicException e) {
            logger.warn("Бизнес-логика не пройдена для групповой заявки: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при создании групповой заявки: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при создании групповой заявки", e);
        }
    }

    /**
     * Создает групповую заявку с возвратом цены инструмента.
     * 
     * @param request данные групповой заявки
     * @return ответ с заявками и ценой инструмента из main_price поля
     */
    @PostMapping("/group/with-price")
    public ResponseEntity<GroupOrderResponseDTO> createGroupOrderWithPrice(
            @RequestBody SingleOrderRequest request) {
        try {
            logger.info("Создание заявки с ценой (один инструмент): instrument={}, время={}",
                    request.instrument(), request.start_time());

            // Обработка через сервис: персистенция + немедленная отправка при "now"
            List<OrderDTO> orders = delayedOrderService.processSingleOrderWithPrice(request);

            if (orders.isEmpty()) {
                logger.warn("Не удалось создать заявки для инструмента");
                throw new BusinessLogicException(
                        "Не удалось создать заявки для инструмента",
                        "ORDERS_NOT_CREATED");
            }

            GroupOrderResponseDTO response = GroupOrderResponseDTO.of(orders, java.math.BigDecimal.ZERO);
            logger.info("Создано {} заявок для инструмента {}", response.orders().size(), request.instrument());
            return ResponseEntity.ok(response);

        } catch (com.example.investmenttradingservice.exception.ValidationException e) {
            logger.warn("Валидация не пройдена для инструмента {}: {}", request.instrument(), e.getMessage());
            throw e;
        } catch (com.example.investmenttradingservice.exception.BusinessLogicException e) {
            logger.warn("Бизнес-логика не пройдена для инструмента {}: {}", request.instrument(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при создании заявки с ценой для инструмента {}: {}", request.instrument(),
                    e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при создании заявки с ценой", e);
        }
    }

    /**
     * Создает лимитные ордера для множественных инструментов.
     * Поддерживает типы лимитов: limitUp и limitDown.
     * 
     * @param request данные лимитного ордера
     * @return ответ с созданными ордерами
     */
    @PostMapping("/by-limit")
    public ResponseEntity<LimitOrderResponse> createLimitOrders(@RequestBody LimitOrderRequest request) {
        try {
            logger.info("Создание лимитных ордеров для {} инструментов, тип лимита: {}, время: {}",
                    request.getInstrumentsCount(), request.levels().level(), request.start_time());

            // Валидация запроса
            if (request.instruments() == null || request.instruments().isEmpty()) {
                throw new ValidationException(
                        "Список инструментов не может быть пустым",
                        "instruments",
                        "empty_list");
            }

            if (request.levels() == null) {
                throw new ValidationException(
                        "Уровни лимитов обязательны",
                        "levels",
                        "null");
            }

            // Обработка через существующий сервис (адаптируем под новую структуру)
            List<OrderDTO> orders = delayedOrderService.processLimitOrders(request);

            if (orders.isEmpty()) {
                logger.warn("Не удалось создать лимитные ордера");
                throw new BusinessLogicException(
                        "Не удалось создать лимитные ордера",
                        "LIMIT_ORDERS_NOT_CREATED");
            }

            LimitOrderResponse response = LimitOrderResponse.of(
                    orders,
                    request.levels().level(),
                    request.getInstrumentsCount());

            logger.info("Создано {} лимитных ордеров типа {}", response.getSuccessCount(), request.levels().level());
            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            logger.warn("Валидация не пройдена для лимитных ордеров: {}", e.getMessage());
            throw e;
        } catch (BusinessLogicException e) {
            logger.warn("Бизнес-логика не пройдена для лимитных ордеров: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при создании лимитных ордеров: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при создании лимитных ордеров", e);
        }
    }

    /**
     * Создает заявки для одного инструмента, когда уровни — это финальные цены.
     */
    @PostMapping("/single/with-price")
    public ResponseEntity<List<OrderDTO>> createSingleOrderWithPrice(
            @RequestBody SingleOrderRequest request) {
        try {
            logger.info("Создание одиночной(группы) заявки для инструмента {}, уровни как цены, время: {}",
                    request.instrument(), request.start_time());

            // Обработка через сервис: персистенция + немедленная отправка при "now"
            List<OrderDTO> orders = delayedOrderService.processSingleOrderWithPrice(request);

            if (orders.isEmpty()) {
                throw new BusinessLogicException("Не удалось создать заявки для инструмента", "ORDERS_NOT_CREATED");
            }

            // Немедленная отправка при now аналогична групповой логике — используем сервис
            // для консистентности: обрабатываем через DelayedOrderService API
            // (валидация и немедленная отправка там уже реализованы, если нужно — можно
            // дублировать)

            return ResponseEntity.ok(orders);
        } catch (com.example.investmenttradingservice.exception.ValidationException e) {
            logger.warn("Валидация не пройдена для инструмента {}: {}", request.instrument(), e.getMessage());
            throw e;
        } catch (com.example.investmenttradingservice.exception.BusinessLogicException e) {
            logger.warn("Бизнес-логика не пройдена для инструмента {}: {}", request.instrument(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при создании заявок для инструмента {}: {}", request.instrument(),
                    e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при создании заявки для инструмента", e);
        }
    }

    /**
     * 
     * 
     * /**
     * Возвращает все заявки, находящиеся в кэше.
     * 
     * @return список заявок из кэша
     */
    @GetMapping("/cache")
    public ResponseEntity<List<OrderDTO>> getCachedOrders() {
        try {
            List<OrderDTO> cached = orderCacheService.getAllDTOs();
            return ResponseEntity.ok(cached);
        } catch (Exception e) {
            logger.error("Ошибка при получении заявок из кэша: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении заявок из кэша", e);
        }
    }

    /**
     * Получает заявку по её ID.
     * 
     * @param orderId ID заявки
     * @return заявка или 404 если не найдена
     */
    @GetMapping("/{orderId:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<ApiSuccessResponse<OrderResponseDTO>> getOrder(@PathVariable String orderId) {
        logger.info("Получен запрос на заявку с ID: {}", orderId);

        // Валидация orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ValidationException(
                    "ID заявки не может быть пустым",
                    "orderId",
                    orderId);
        }

        // Проверка формата UUID
        if (!orderId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            throw new ValidationException(
                    "ID заявки должен быть в формате UUID",
                    "orderId",
                    orderId);
        }

        try {
            return orderPersistenceService.findOrderByOrderId(orderId)
                    .map(entity -> {
                        logger.info("Заявка с ID {} найдена", orderId);

                        OrderResponseDTO orderResponseDTO = OrderResponseDTO.fromEntity(entity);

                        ApiSuccessResponse<OrderResponseDTO> response = ApiSuccessResponse.<OrderResponseDTO>builder()
                                .message("Заявка успешно найдена")
                                .data(orderResponseDTO)
                                .totalCount(1)
                                .dataType("order")
                                .addMetadata("orderId", orderId)
                                .addMetadata("status", orderResponseDTO.status().toString())
                                .addMetadata("isFinalStatus", orderResponseDTO.isFinalStatus())
                                .addMetadata("hasError", orderResponseDTO.hasError())
                                .build();

                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        logger.warn("Заявка с ID {} не найдена", orderId);
                        throw new BusinessLogicException(
                                String.format("Заявка с ID '%s' не найдена", orderId),
                                "ORDER_NOT_FOUND");
                    });
        } catch (ValidationException | BusinessLogicException e) {
            // Эти исключения обрабатываются глобальным обработчиком
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении заявки {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении заявки", e);
        }
    }

    /**
     * Принудительно отправляет заявку (не дожидаясь scheduled time).
     * 
     * @param orderId ID заявки для отправки
     * @return результат отправки
     */
    @PostMapping("/{orderId:[0-9a-fA-F\\-]{36}}/send")
    public ResponseEntity<ApiSuccessResponse<TinkoffPostOrderResponseDTO>> sendOrder(@PathVariable String orderId) {
        logger.info("Принудительная отправка заявки ID: {}", orderId);

        // Валидация orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ValidationException(
                    "ID заявки не может быть пустым",
                    "orderId",
                    orderId);
        }

        // Проверка формата UUID
        if (!orderId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            throw new ValidationException(
                    "ID заявки должен быть в формате UUID",
                    "orderId",
                    orderId);
        }

        try {
            var raw = orderSchedulerService.forceSendOrderRaw(orderId);
            var dto = TinkoffPostOrderResponseDTO.from(raw);
            ApiSuccessResponse<TinkoffPostOrderResponseDTO> response = ApiSuccessResponse
                    .<TinkoffPostOrderResponseDTO>builder()
                    .message("PostOrder выполнен")
                    .data(dto)
                    .totalCount(1)
                    .dataType("tinvest_post_order_response")
                    .addMetadata("orderId", orderId)
                    .addMetadata("operation", "send_order_raw")
                    .build();
            return ResponseEntity.ok(response);

        } catch (ValidationException | BusinessLogicException e) {
            // Эти исключения обрабатываются глобальным обработчиком
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при принудительной отправке заявки {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при отправке заявки", e);
        }
    }

    /**
     * Отменяет заявку по её UUID.
     * 
     * @param orderId UUID заявки
     * @return результат отмены
     */
    @PostMapping("/{orderId:[0-9a-fA-F\\-]{36}}/cancel")
    public ResponseEntity<ApiSuccessResponse<String>> cancelOrder(@PathVariable String orderId) {
        logger.info("Отмена заявки ID: {}", orderId);

        // Валидация orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ValidationException(
                    "ID заявки не может быть пустым",
                    "orderId",
                    orderId);
        }

        // Проверка формата UUID
        if (!orderId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            throw new ValidationException(
                    "ID заявки должен быть в формате UUID",
                    "orderId",
                    orderId);
        }

        try {
            if (!orderPersistenceService.orderExists(orderId)) {
                throw new BusinessLogicException(
                        String.format("Заявка с ID '%s' не найдена для отмены", orderId),
                        "ORDER_NOT_FOUND");
            }

            boolean updated = orderPersistenceService.updateOrderStatus(orderId, OrderStatus.CANCELLED, null, null);
            if (updated) {
                // Удаляем заявку из кэша, чтобы планировщик не обработал её
                try {
                    orderCacheService.remove(orderId);
                } catch (Exception removeEx) {
                    logger.warn("Не удалось удалить заявку {} из кэша после отмены: {}", orderId,
                            removeEx.getMessage());
                }
                ApiSuccessResponse<String> response = ApiSuccessResponse.<String>builder()
                        .message("Заявка успешно отменена")
                        .data("Заявка отменена и больше не будет отправлена")
                        .totalCount(1)
                        .dataType("operation_result")
                        .addMetadata("orderId", orderId)
                        .addMetadata("operation", "cancel_order")
                        .addMetadata("newStatus", OrderStatus.CANCELLED.toString())
                        .build();

                return ResponseEntity.ok(response);
            } else {
                throw new BusinessLogicException(
                        "Не удалось отменить заявку",
                        "ORDER_CANCEL_FAILED");
            }

        } catch (ValidationException | BusinessLogicException e) {
            // Эти исключения обрабатываются глобальным обработчиком
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при отмене заявки {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при отмене заявки", e);
        }
    }

    /**
     * Удаляет заявку по её UUID.
     *
     * @param orderId UUID заявки
     * @return результат удаления
     */
    @DeleteMapping("/{orderId:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<ApiSuccessResponse<String>> deleteOrder(@PathVariable String orderId) {
        logger.info("Удаление заявки ID: {}", orderId);

        // Валидация orderId
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new ValidationException(
                    "ID заявки не может быть пустым",
                    "orderId",
                    orderId);
        }

        // Проверка формата UUID
        if (!orderId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            throw new ValidationException(
                    "ID заявки должен быть в формате UUID",
                    "orderId",
                    orderId);
        }

        try {
            if (!orderPersistenceService.orderExists(orderId)) {
                throw new BusinessLogicException(
                        String.format("Заявка с ID '%s' не найдена для удаления", orderId),
                        "ORDER_NOT_FOUND");
            }

            boolean deleted = orderPersistenceService.deleteOrder(orderId);

            if (deleted) {
                // Удаляем заявку из кэша, если она там есть
                try {
                    orderCacheService.remove(orderId);
                } catch (Exception removeEx) {
                    logger.warn("Не удалось удалить заявку {} из кэша после удаления из БД: {}", orderId,
                            removeEx.getMessage());
                }
                ApiSuccessResponse<String> response = ApiSuccessResponse.<String>builder()
                        .message("Заявка успешно удалена")
                        .data("Заявка полностью удалена из системы")
                        .totalCount(1)
                        .dataType("operation_result")
                        .addMetadata("orderId", orderId)
                        .addMetadata("operation", "delete_order")
                        .build();

                return ResponseEntity.ok(response);
            } else {
                throw new BusinessLogicException(
                        "Не удалось удалить заявку",
                        "ORDER_DELETE_FAILED");
            }

        } catch (ValidationException | BusinessLogicException e) {
            // Эти исключения обрабатываются глобальным обработчиком
            throw e;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при удалении заявки {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при удалении заявки", e);
        }
    }

    /**
     * Получает заявки по статусу.
     * 
     * @param status статус заявок
     * @return список заявок с указанным статусом
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderDTO> orderDTOs = orderPersistenceService.findOrdersByStatusDTO(orderStatus);
            return ResponseEntity.ok(orderDTOs);

        } catch (IllegalArgumentException e) {
            logger.warn("Некорректный статус заявки: {}", status);
            throw new ValidationException(
                    String.format("Некорректный статус заявки: %s", status),
                    "status",
                    status);
        } catch (Exception e) {
            logger.error("Ошибка при получении заявок по статусу {}: {}", status, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении заявок по статусу", e);
        }
    }

    /**
     * Получает заявки, готовые к отправке в указанное время.
     * 
     * @param time время для проверки (формат HH:mm:ss)
     * @return список заявок готовых к отправке
     */
    @GetMapping("/ready/{time}")
    public ResponseEntity<List<OrderDTO>> getOrdersReadyToSend(@PathVariable String time) {
        try {
            LocalTime scheduledTime = LocalTime.parse(time);
            List<OrderDTO> orderDTOs = orderPersistenceService.findOrdersReadyToSendDTO(scheduledTime);
            return ResponseEntity.ok(orderDTOs);

        } catch (Exception e) {
            logger.error("Ошибка при получении заявок готовых к отправке в {}: {}", time, e.getMessage(), e);
            throw new ValidationException(
                    String.format("Некорректный формат времени: %s (ожидается HH:mm:ss)", time),
                    "time",
                    time);
        }
    }

    /**
     * Получает статистику по заявкам.
     * 
     * @return статистика заявок
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiSuccessResponse<String>> getOrdersStatistics() {
        try {
            String statistics = orderPersistenceService.getOrdersStatistics();

            ApiSuccessResponse<String> response = ApiSuccessResponse.<String>builder()
                    .message("Статистика заявок успешно получена")
                    .data(statistics)
                    .totalCount(1)
                    .dataType("statistics")
                    .addMetadata("operation", "get_statistics")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении статистики заявок: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении статистики", e);
        }
    }

    /**
     * Получает статистику планировщика.
     * 
     * @return статистика планировщика
     */
    @GetMapping("/scheduler/statistics")
    public ResponseEntity<ApiSuccessResponse<String>> getSchedulerStatistics() {
        try {
            String statistics = orderSchedulerService.getSchedulerStatistics();

            ApiSuccessResponse<String> response = ApiSuccessResponse.<String>builder()
                    .message("Статистика планировщика успешно получена")
                    .data(statistics)
                    .totalCount(1)
                    .dataType("scheduler_statistics")
                    .addMetadata("operation", "get_scheduler_statistics")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении статистики планировщика: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении статистики планировщика", e);
        }
    }

    /**
     * Получает заявки с ошибками.
     * 
     * @return список заявок с ошибками
     */
    @GetMapping("/errors")
    public ResponseEntity<List<OrderDTO>> getOrdersWithErrors() {
        try {
            List<OrderDTO> orderDTOs = orderPersistenceService.findOrdersWithErrorsDTO();
            return ResponseEntity.ok(orderDTOs);

        } catch (Exception e) {
            logger.error("Ошибка при получении заявок с ошибками: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка при получении заявок с ошибками", e);
        }
    }

}
