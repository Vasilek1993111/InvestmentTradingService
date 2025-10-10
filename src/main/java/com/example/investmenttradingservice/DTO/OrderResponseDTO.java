package com.example.investmenttradingservice.DTO;

import java.time.LocalTime;

import com.example.investmenttradingservice.enums.OrderDirection;
import com.example.investmenttradingservice.enums.OrderStatus;
import com.example.investmenttradingservice.enums.OrderType;

/**
 * DTO для ответов с информацией о заявке.
 * Включает статус заявки и дополнительную информацию для API ответов.
 * 
 * @param quantity       количество лотов
 * @param price          котировка цены (units + nano)
 * @param direction      направление операции
 * @param accountId      идентификатор аккаунта
 * @param orderType      тип заявки
 * @param orderId        уникальный идентификатор заявки (UUID)
 * @param instrumentId   идентификатор инструмента (FIGI)
 * @param scheduledTime  время исполнения заявки
 * @param status         текущий статус заявки
 * @param tinvestOrderId идентификатор заявки в T-Invest API
 * @param errorMessage   сообщение об ошибке (если есть)
 */
public record OrderResponseDTO(
        int quantity,
        QuotationDTO price,
        OrderDirection direction,
        String accountId,
        OrderType orderType,
        String orderId,
        String instrumentId,
        LocalTime scheduledTime,
        OrderStatus status,
        String tinvestOrderId,
        String errorMessage) {

    /**
     * Создает OrderResponseDTO из OrderEntity.
     * 
     * @param entity сущность заявки
     * @return DTO для ответа
     */
    public static OrderResponseDTO fromEntity(com.example.investmenttradingservice.entity.OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        // Создаем QuotationDTO из BigDecimal цены
        QuotationDTO price = null;
        if (entity.getPrice() != null) {
            price = QuotationDTO.fromBigDecimal(entity.getPrice());
        }

        return new OrderResponseDTO(
                entity.getQuantity(),
                price,
                entity.getDirection(),
                entity.getAccountId(),
                entity.getOrderType(),
                entity.getOrderId(),
                entity.getInstrumentId(),
                entity.getScheduledTime(),
                entity.getStatus(),
                entity.getTinvestOrderId(),
                entity.getErrorMessage());
    }

    /**
     * Создает OrderResponseDTO из OrderDTO с добавлением статуса.
     * 
     * @param orderDTO       базовый DTO заявки
     * @param status         статус заявки
     * @param tinvestOrderId идентификатор в T-Invest API
     * @param errorMessage   сообщение об ошибке
     * @return DTO для ответа
     */
    public static OrderResponseDTO fromOrderDTO(OrderDTO orderDTO, OrderStatus status,
            String tinvestOrderId, String errorMessage) {
        if (orderDTO == null) {
            return null;
        }

        return new OrderResponseDTO(
                orderDTO.quantity(),
                orderDTO.price(),
                orderDTO.direction(),
                orderDTO.accountId(),
                orderDTO.orderType(),
                orderDTO.orderId(),
                orderDTO.instrumentId(),
                orderDTO.scheduledTime(),
                status,
                tinvestOrderId,
                errorMessage);
    }

    /**
     * Проверяет, является ли заявка валидной.
     * 
     * @return true если все поля заполнены корректно
     */
    public boolean isValid() {
        return quantity > 0
                && price != null
                && !price.isZero()
                && direction != null
                && accountId != null
                && !accountId.trim().isEmpty()
                && orderId != null
                && !orderId.trim().isEmpty()
                && instrumentId != null
                && !instrumentId.trim().isEmpty()
                && scheduledTime != null
                && status != null;
    }

    /**
     * Проверяет, является ли заявка в финальном статусе.
     * 
     * @return true если заявка исполнена, отклонена или отменена
     */
    public boolean isFinalStatus() {
        return status == OrderStatus.EXECUTED
                || status == OrderStatus.REJECTED
                || status == OrderStatus.CANCELLED;
    }

    /**
     * Проверяет, является ли заявка готовой к отправке.
     * 
     * @return true если заявка в статусе PENDING
     */
    public boolean isReadyToSend() {
        return status == OrderStatus.PENDING;
    }

    /**
     * Проверяет, есть ли ошибка в заявке.
     * 
     * @return true если статус ERROR или есть сообщение об ошибке
     */
    public boolean hasError() {
        return status == OrderStatus.ERROR || (errorMessage != null && !errorMessage.trim().isEmpty());
    }
}
