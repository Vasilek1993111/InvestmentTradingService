package com.example.investmenttradingservice.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.investmenttradingservice.entity.OrderEntity;
import com.example.investmenttradingservice.enums.OrderStatus;

/**
 * Repository для работы с заявками в базе данных.
 * Предоставляет методы для CRUD операций и поиска заявок.
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    /**
     * Находит заявку по orderId (UUID).
     * 
     * @param orderId идентификатор заявки
     * @return Optional с заявкой
     */
    Optional<OrderEntity> findByOrderId(String orderId);

    /**
     * Находит все заявки по статусу.
     * 
     * @param status статус заявки
     * @return список заявок с указанным статусом
     */
    List<OrderEntity> findByStatus(OrderStatus status);

    /**
     * Находит все заявки по инструменту.
     * 
     * @param instrumentId идентификатор инструмента
     * @return список заявок для инструмента
     */
    List<OrderEntity> findByInstrumentId(String instrumentId);

    /**
     * Находит все заявки по аккаунту.
     * 
     * @param accountId идентификатор аккаунта
     * @return список заявок для аккаунта
     */
    List<OrderEntity> findByAccountId(String accountId);

    /**
     * Находит заявки, готовые к отправке в указанное время.
     * 
     * @param scheduledTime время исполнения
     * @param status        статус заявки
     * @return список заявок, готовых к отправке
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.scheduledTime <= :scheduledTime AND o.status = :status")
    List<OrderEntity> findOrdersReadyToSend(@Param("scheduledTime") LocalTime scheduledTime,
            @Param("status") OrderStatus status);

    /**
     * Находит заявки по статусу и инструменту.
     * 
     * @param status       статус заявки
     * @param instrumentId идентификатор инструмента
     * @return список заявок
     */
    List<OrderEntity> findByStatusAndInstrumentId(OrderStatus status, String instrumentId);

    /**
     * Подсчитывает количество заявок по статусу.
     * 
     * @param status статус заявки
     * @return количество заявок
     */
    long countByStatus(OrderStatus status);

    /**
     * Проверяет существование заявки по orderId.
     * 
     * @param orderId идентификатор заявки
     * @return true если заявка существует
     */
    boolean existsByOrderId(String orderId);

    /**
     * Находит заявки, созданные в указанном временном диапазоне.
     * 
     * @param startTime время начала диапазона
     * @param endTime   время окончания диапазона
     * @return список заявок
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt <= :endTime")
    List<OrderEntity> findOrdersCreatedBetween(@Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * Находит заявки по статусу и времени исполнения.
     * 
     * @param status        статус заявки
     * @param scheduledTime время исполнения
     * @return список заявок
     */
    List<OrderEntity> findByStatusAndScheduledTime(OrderStatus status, LocalTime scheduledTime);

    /**
     * Находит заявки с ошибками.
     * 
     * @return список заявок со статусом ERROR
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.status = 'ERROR' OR o.errorMessage IS NOT NULL")
    List<OrderEntity> findOrdersWithErrors();

    /**
     * Находит заявки, которые нужно отправить в ближайшее время.
     * 
     * @param currentTime текущее время
     * @param status      статус заявки
     * @return список заявок для отправки
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.scheduledTime <= :currentTime AND o.status = :status ORDER BY o.scheduledTime ASC")
    List<OrderEntity> findOrdersToSendNow(@Param("currentTime") LocalTime currentTime,
            @Param("status") OrderStatus status);
}