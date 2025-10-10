
package com.example.investmenttradingservice.entity;

import com.example.investmenttradingservice.enums.OrderDirection;
import com.example.investmenttradingservice.enums.OrderStatus;
import com.example.investmenttradingservice.enums.OrderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Сущность для хранения заявок в базе данных.
 * Представляет заявку на покупку/продажу инструментов через T-Invest API.
 * 
 * <p>
 * Основные характеристики:
 * </p>
 * <ul>
 * <li>ID генерируется пользователем (UUID)</li>
 * <li>Поддерживает все типы заявок T-Invest API</li>
 * <li>Отслеживает статус выполнения заявки</li>
 * <li>Сохраняет временные метки создания и обновления</li>
 * <li>Thread-safe для использования в многопоточной среде</li>
 * </ul>
 * 
 * @author Investment Trading Service
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "orders", schema = "invest", indexes = {
        @Index(name = "idx_order_account_id", columnList = "accountId"),
        @Index(name = "idx_order_instrument_id", columnList = "instrumentId"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_scheduled_time", columnList = "scheduledTime"),
        @Index(name = "idx_order_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    /**
     * Уникальный идентификатор заявки.
     * Генерируется пользователем в виде UUID.
     */
    @Id
    @Column(name = "order_id", nullable = false, length = 36)
    @Size(max = 36, message = "Order ID не должен превышать 36 символов")
    @NotNull(message = "Order ID не может быть null")
    private String orderId;

    /**
     * Количество лотов для торговли.
     * Должно быть положительным числом.
     */
    @Column(name = "quantity", nullable = false)
    @Positive(message = "Количество лотов должно быть положительным")
    @NotNull(message = "Количество лотов не может быть null")
    private Integer quantity;

    /**
     * Цена заявки в виде BigDecimal.
     * Сохраняется с точностью 18 знаков до запятой и 9 после.
     */
    @Column(name = "price", nullable = false, precision = 18, scale = 9)
    @NotNull(message = "Цена не может быть null")
    private BigDecimal price;

    /**
     * Направление операции (покупка/продажа).
     * Использует enum OrderDirection.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 50)
    @NotNull(message = "Направление операции не может быть null")
    private OrderDirection direction;

    /**
     * Идентификатор торгового аккаунта.
     * Связывает заявку с конкретным аккаунтом пользователя.
     */
    @Column(name = "account_id", nullable = false, length = 255)
    @Size(max = 255, message = "Account ID не должен превышать 255 символов")
    @NotNull(message = "Account ID не может быть null")
    private String accountId;

    /**
     * Тип заявки (лимитная/рыночная).
     * Использует enum OrderType.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 50)
    @NotNull(message = "Тип заявки не может быть null")
    private OrderType orderType;

    /**
     * Идентификатор инструмента (FIGI).
     * Ссылается на конкретный финансовый инструмент.
     */
    @Column(name = "instrument_id", nullable = false, length = 50)
    @Size(max = 50, message = "Instrument ID не должен превышать 50 символов")
    @NotNull(message = "Instrument ID не может быть null")
    private String instrumentId;

    /**
     * Запланированное время исполнения заявки.
     * Используется для отложенных заявок.
     */
    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    /**
     * Текущий статус заявки.
     * Отслеживает жизненный цикл заявки от создания до исполнения.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Статус заявки не может быть null")
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Идентификатор заявки в системе T-Invest API.
     * Заполняется после успешной отправки заявки.
     */
    @Column(name = "tinvest_order_id", length = 100)
    @Size(max = 100, message = "T-Invest Order ID не должен превышать 100 символов")
    private String tinvestOrderId;

    /**
     * Сообщение об ошибке при обработке заявки.
     * Заполняется в случае неудачного исполнения.
     */
    @Column(name = "error_message", length = 1000)
    @Size(max = 1000, message = "Сообщение об ошибке не должно превышать 1000 символов")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Время последнего обновления записи.
     * Автоматически обновляется при изменении.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Конструктор для создания новой заявки.
     * 
     * @param orderId       уникальный идентификатор заявки
     * @param quantity      количество лотов
     * @param price         цена заявки
     * @param direction     направление операции
     * @param accountId     идентификатор аккаунта
     * @param orderType     тип заявки
     * @param instrumentId  идентификатор инструмента
     * @param scheduledTime запланированное время исполнения
     */
    public OrderEntity(String orderId, Integer quantity, BigDecimal price,
            OrderDirection direction, String accountId, OrderType orderType,
            String instrumentId, LocalTime scheduledTime) {
        this.orderId = orderId;
        this.quantity = quantity;
        this.price = price;
        this.direction = direction;
        this.accountId = accountId;
        this.orderType = orderType;
        this.instrumentId = instrumentId;
        this.scheduledTime = scheduledTime;
        this.status = OrderStatus.PENDING;

    }

    /**
     * Проверяет, является ли заявка готовой к отправке.
     * 
     * @return true если заявка в статусе PENDING и все обязательные поля заполнены
     */
    public boolean isReadyToSend() {
        return status == OrderStatus.PENDING
                && orderId != null
                && quantity != null
                && quantity > 0
                && price != null
                && direction != null
                && accountId != null
                && orderType != null
                && instrumentId != null;
    }

    /**
     * Проверяет, является ли заявка исполненной или отмененной.
     * 
     * @return true если заявка в финальном статусе
     */
    public boolean isFinalStatus() {
        return status == OrderStatus.EXECUTED
                || status == OrderStatus.REJECTED
                || status == OrderStatus.CANCELLED;
    }

    /**
     * Обновляет статус заявки и очищает сообщение об ошибке при успешной отправке.
     * 
     * @param tinvestOrderId идентификатор заявки в T-Invest API
     */
    public void markAsSent(String tinvestOrderId) {
        this.status = OrderStatus.SENT;
        this.tinvestOrderId = tinvestOrderId;
        this.errorMessage = null;
    }

    /**
     * Отмечает заявку как исполненную.
     */
    public void markAsExecuted() {
        this.status = OrderStatus.EXECUTED;
        this.errorMessage = null;
    }

    /**
     * Отмечает заявку как отклоненную с указанием причины.
     * 
     * @param errorMessage причина отклонения
     */
    public void markAsRejected(String errorMessage) {
        this.status = OrderStatus.REJECTED;
        this.errorMessage = errorMessage;
    }

    /**
     * Отмечает заявку как отмененную.
     */
    public void markAsCancelled() {
        this.status = OrderStatus.CANCELLED;
        this.errorMessage = null;
    }

    /**
     * Отмечает заявку как ошибочную с указанием причины.
     * 
     * @param errorMessage описание ошибки
     */
    public void markAsError(String errorMessage) {
        this.status = OrderStatus.ERROR;
        this.errorMessage = errorMessage;
    }

    /**
     * Проверяет, является ли заявка покупкой.
     * 
     * @return true если направление - покупка
     */
    public boolean isBuyOrder() {
        return direction == OrderDirection.ORDER_DIRECTION_BUY;
    }

    /**
     * Проверяет, является ли заявка продажей.
     * 
     * @return true если направление - продажа
     */
    public boolean isSellOrder() {
        return direction == OrderDirection.ORDER_DIRECTION_SELL;
    }

    /**
     * Проверяет, является ли заявка лимитной.
     * 
     * @return true если тип заявки - лимитная
     */
    public boolean isLimitOrder() {
        return orderType == OrderType.ORDER_TYPE_LIMIT;
    }

    /**
     * Проверяет, является ли заявка рыночной.
     * 
     * @return true если тип заявки - рыночная
     */
    public boolean isMarketOrder() {
        return orderType == OrderType.ORDER_TYPE_MARKET;
    }

    /**
     * Получает общую стоимость заявки.
     * 
     * @return общая стоимость (количество * цена)
     */
    public BigDecimal getTotalValue() {
        if (quantity == null || price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
