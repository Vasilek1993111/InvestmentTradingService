package com.example.investmenttradingservice.mapper;

import org.springframework.stereotype.Component;

import com.example.investmenttradingservice.DTO.OrderDTO;
import com.example.investmenttradingservice.DTO.QuotationDTO;
import com.example.investmenttradingservice.entity.OrderEntity;
import com.example.investmenttradingservice.enums.OrderStatus;

/**
 * Маппер для преобразования между OrderEntity и OrderDTO.
 */
@Component
public class OrderMapper {

    /**
     * Преобразует OrderDTO в OrderEntity.
     * 
     * @param orderDTO DTO заявки
     * @return Entity заявки
     */
    public OrderEntity toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return null;
        }

        OrderEntity entity = new OrderEntity();
        entity.setOrderId(orderDTO.orderId());
        entity.setQuantity(orderDTO.quantity());

        // Преобразуем цену из QuotationDTO в BigDecimal
        if (orderDTO.price() != null) {
            entity.setPrice(orderDTO.price().toBigDecimal());
        }

        entity.setDirection(orderDTO.direction());
        entity.setAccountId(orderDTO.accountId());
        entity.setOrderType(orderDTO.orderType());
        entity.setInstrumentId(orderDTO.instrumentId());
        entity.setScheduledTime(orderDTO.scheduledTime());
        entity.setStatus(OrderStatus.PENDING); // По умолчанию статус PENDING

        return entity;
    }

    /**
     * Преобразует OrderEntity в OrderDTO.
     * 
     * @param entity Entity заявки
     * @return DTO заявки
     */
    public OrderDTO toDTO(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        // Создаем QuotationDTO из BigDecimal цены
        QuotationDTO price = null;
        if (entity.getPrice() != null) {
            price = QuotationDTO.fromBigDecimal(entity.getPrice());
        }

        return new OrderDTO(
                entity.getQuantity(),
                price,
                entity.getDirection(),
                entity.getAccountId(),
                entity.getOrderType(),
                entity.getOrderId(),
                entity.getInstrumentId(),
                entity.getScheduledTime());
    }

    /**
     * Обновляет существующий Entity данными из DTO.
     * 
     * @param entity   существующий Entity
     * @param orderDTO DTO с новыми данными
     * @return обновленный Entity
     */
    public OrderEntity updateEntity(OrderEntity entity, OrderDTO orderDTO) {
        if (entity == null || orderDTO == null) {
            return entity;
        }

        entity.setQuantity(orderDTO.quantity());

        // Обновляем цену
        if (orderDTO.price() != null) {
            entity.setPrice(orderDTO.price().toBigDecimal());
        }

        entity.setDirection(orderDTO.direction());
        entity.setAccountId(orderDTO.accountId());
        entity.setOrderType(orderDTO.orderType());
        entity.setInstrumentId(orderDTO.instrumentId());
        entity.setScheduledTime(orderDTO.scheduledTime());

        return entity;
    }

    // Удалены вспомогательные методы, не требуемые доменной логикой

    /**
     * Создает OrderEntity из OrderDTO с использованием конструктора.
     * 
     * @param orderDTO DTO заявки
     * @return Entity заявки
     */

    /**
     * Проверяет валидность данных перед маппингом.
     * 
     * @param orderDTO DTO для проверки
     * @return true если данные валидны
     */

    /**
     * Проверяет валидность данных перед маппингом.
     * 
     * @param entity Entity для проверки
     * @return true если данные валидны
     */

}
