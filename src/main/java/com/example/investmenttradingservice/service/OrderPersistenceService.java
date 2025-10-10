package com.example.investmenttradingservice.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.investmenttradingservice.DTO.OrderDTO;
import com.example.investmenttradingservice.entity.OrderEntity;
import com.example.investmenttradingservice.enums.OrderStatus;
import com.example.investmenttradingservice.mapper.OrderMapper;
import com.example.investmenttradingservice.repository.OrderRepository;

/**
 * Сервис для работы с заявками в базе данных.
 * Предоставляет методы для сохранения, поиска и управления заявками.
 */
@Service
@Transactional
public class OrderPersistenceService {

    /** Логгер для записи операций сервиса */
    private static final Logger logger = LoggerFactory.getLogger(OrderPersistenceService.class);

    /** Репозиторий для работы с заявками */
    @Autowired
    private OrderRepository orderRepository;

    /** Маппер для преобразования Entity <-> DTO */
    @Autowired
    private OrderMapper orderMapper;

    /**
     * Сохраняет заявку в базе данных.
     * 
     * @param orderDTO DTO заявки
     * @return сохраненная заявка как Entity
     */
    public OrderEntity saveOrder(OrderDTO orderDTO) {
        try {
            logger.debug("Сохранение заявки: {}", orderDTO.orderId());

            OrderEntity entity = orderMapper.toEntity(orderDTO);
            OrderEntity savedEntity = orderRepository.save(entity);

            logger.info("Заявка сохранена в БД: OrderID={}, Instrument={}",
                    savedEntity.getOrderId(), savedEntity.getInstrumentId());

            return savedEntity;

        } catch (Exception e) {
            logger.error("Ошибка при сохранении заявки {}: {}", orderDTO.orderId(), e.getMessage(), e);
            throw new RuntimeException("Не удалось сохранить заявку", e);
        }
    }

    /**
     * Сохраняет список заявок в базе данных.
     * 
     * @param orders список DTO заявок
     * @return список сохраненных Entity
     */
    public List<OrderEntity> saveOrders(List<OrderDTO> orders) {
        if (orders == null || orders.isEmpty()) {
            logger.warn("Список заявок для сохранения пуст");
            return List.of();
        }

        logger.info("Сохранение {} заявок в БД", orders.size());

        try {
            List<OrderEntity> entities = orders.stream()
                    .map(orderMapper::toEntity)
                    .toList();

            List<OrderEntity> savedEntities = orderRepository.saveAll(entities);

            logger.info("Успешно сохранено {} заявок в БД", savedEntities.size());

            return savedEntities;

        } catch (Exception e) {
            logger.error("Ошибка при сохранении списка заявок: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось сохранить заявки", e);
        }
    }

    /**
     * Находит заявку по orderId.
     * 
     * @param orderId идентификатор заявки
     * @return Optional с заявкой
     */
    @Transactional(readOnly = true)
    public Optional<OrderEntity> findOrderByOrderId(String orderId) {
        try {
            return orderRepository.findByOrderId(orderId);
        } catch (Exception e) {
            logger.error("Ошибка при поиске заявки {}: {}", orderId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Возвращает заявку по ID в виде DTO.
     *
     * @param orderId идентификатор заявки
     * @return Optional с DTO
     */
    @Transactional(readOnly = true)
    public Optional<OrderDTO> findOrderByOrderIdDTO(String orderId) {
        return findOrderByOrderId(orderId).map(orderMapper::toDTO);
    }

    /**
     * Находит все заявки по статусу.
     * 
     * @param status статус заявки
     * @return список заявок
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> findOrdersByStatus(OrderStatus status) {
        try {
            return orderRepository.findByStatus(status);
        } catch (Exception e) {
            logger.error("Ошибка при поиске заявок по статусу {}: {}", status, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Возвращает заявки по статусу в виде DTO.
     *
     * @param status статус заявки
     * @return список DTO
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> findOrdersByStatusDTO(OrderStatus status) {
        return findOrdersByStatus(status).stream().map(orderMapper::toDTO).toList();
    }

    /**
     * Находит заявки, готовые к отправке в указанное время.
     * 
     * @param scheduledTime время исполнения
     * @return список заявок, готовых к отправке
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> findOrdersReadyToSend(LocalTime scheduledTime) {
        try {
            return orderRepository.findOrdersReadyToSend(scheduledTime, OrderStatus.PENDING);
        } catch (Exception e) {
            logger.error("Ошибка при поиске заявок для отправки: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Находит заявки, готовые к отправке, и возвращает их как DTO.
     *
     * @param scheduledTime время исполнения
     * @return список DTO
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> findOrdersReadyToSendDTO(LocalTime scheduledTime) {
        return findOrdersReadyToSend(scheduledTime).stream().map(orderMapper::toDTO).toList();
    }

    /**
     * Обновляет статус заявки.
     * 
     * @param orderId      идентификатор заявки
     * @param status       новый статус
     * @param apiResponse  ответ от API (опционально)
     * @param errorMessage сообщение об ошибке (опционально)
     * @return true если статус обновлен
     */
    public boolean updateOrderStatus(String orderId, OrderStatus status, String apiResponse, String errorMessage) {
        try {
            Optional<OrderEntity> orderOpt = orderRepository.findByOrderId(orderId);

            if (orderOpt.isEmpty()) {
                logger.warn("Заявка {} не найдена для обновления статуса", orderId);
                return false;
            }

            OrderEntity entity = orderOpt.get();
            entity.setStatus(status);
            // updatedAt обновится автоматически через @UpdateTimestamp

            if (apiResponse != null) {
                entity.setTinvestOrderId(apiResponse);
            }

            if (errorMessage != null) {
                entity.setErrorMessage(errorMessage);
            }

            orderRepository.save(entity);

            logger.info("Статус заявки {} обновлен на {}", orderId, status);
            return true;

        } catch (Exception e) {
            logger.error("Ошибка при обновлении статуса заявки {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Удаляет заявку по её идентификатору.
     *
     * @param orderId идентификатор заявки (UUID)
     * @return true если удаление выполнено
     */
    public boolean deleteOrder(String orderId) {
        try {
            Optional<OrderEntity> orderOpt = orderRepository.findByOrderId(orderId);
            if (orderOpt.isEmpty()) {
                logger.warn("Заявка {} не найдена для удаления", orderId);
                return false;
            }

            orderRepository.delete(orderOpt.get());
            logger.info("Заявка {} удалена", orderId);
            return true;
        } catch (Exception e) {
            logger.error("Ошибка при удалении заявки {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Получает статистику по заявкам.
     * 
     * @return строка со статистикой
     */
    @Transactional(readOnly = true)
    public String getOrdersStatistics() {
        try {
            long total = orderRepository.count();
            long pending = orderRepository.countByStatus(OrderStatus.PENDING);
            long sent = orderRepository.countByStatus(OrderStatus.SENT);
            long executed = orderRepository.countByStatus(OrderStatus.EXECUTED);
            long rejected = orderRepository.countByStatus(OrderStatus.REJECTED);
            long error = orderRepository.countByStatus(OrderStatus.ERROR);

            return String.format(
                    "Всего заявок: %d (ожидает: %d, отправлено: %d, исполнено: %d, отклонено: %d, ошибок: %d)",
                    total, pending, sent, executed, rejected, error);

        } catch (Exception e) {
            logger.error("Ошибка при получении статистики заявок: {}", e.getMessage(), e);
            return "Ошибка при получении статистики";
        }
    }

    /**
     * Находит заявки с ошибками.
     * 
     * @return список заявок с ошибками
     */
    @Transactional(readOnly = true)
    public List<OrderEntity> findOrdersWithErrors() {
        try {
            return orderRepository.findOrdersWithErrors();
        } catch (Exception e) {
            logger.error("Ошибка при поиске заявок с ошибками: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Возвращает заявки с ошибками в виде DTO.
     *
     * @return список DTO
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> findOrdersWithErrorsDTO() {
        return findOrdersWithErrors().stream().map(orderMapper::toDTO).toList();
    }

    /**
     * Проверяет существование заявки по orderId.
     * 
     * @param orderId идентификатор заявки
     * @return true если заявка существует
     */
    @Transactional(readOnly = true)
    public boolean orderExists(String orderId) {
        try {
            return orderRepository.existsByOrderId(orderId);
        } catch (Exception e) {
            logger.error("Ошибка при проверке существования заявки {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Получает маппер для преобразования Entity <-> DTO.
     * 
     * @return OrderMapper
     */
    // Контроллеры не должны напрямую получать маппер

}
