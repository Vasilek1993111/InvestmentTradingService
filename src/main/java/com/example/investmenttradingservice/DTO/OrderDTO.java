package com.example.investmenttradingservice.DTO;

import java.time.LocalTime;
import java.util.UUID;

import com.example.investmenttradingservice.enums.OrderDirection;
import com.example.investmenttradingservice.enums.OrderType;

/**
 * DTO для создания заявки в T-Invest API.
 * Представляет одиночную заявку с полным набором параметров.
 * 
 * @param quantity      количество лотов
 * @param price         котировка цены (units + nano)
 * @param direction     направление операции
 *                      (ORDER_DIRECTION_BUY/ORDER_DIRECTION_SELL)
 * @param accountId     идентификатор аккаунта
 * @param orderType     тип заявки (по умолчанию ORDER_TYPE_UNSPECIFIED)
 * @param orderId       уникальный идентификатор заявки (UUID)
 * @param instrumentId  идентификатор инструмента (FIGI)
 * @param scheduledTime время исполнения заявки
 */
public record OrderDTO(
        int quantity,
        QuotationDTO price,
        OrderDirection direction,
        String accountId,
        OrderType orderType,
        String orderId,
        String instrumentId,
        LocalTime scheduledTime) {

    /**
     * Создает OrderDTO с автоматической генерацией UUID.
     * 
     * @param quantity      количество лотов
     * @param price         цена в BigDecimal
     * @param direction     направление операции
     * @param accountId     идентификатор аккаунта
     * @param instrumentId  идентификатор инструмента
     * @param scheduledTime время исполнения заявки
     * @return OrderDTO объект
     */
    public static OrderDTO create(int quantity, java.math.BigDecimal price, OrderDirection direction,
            String accountId, String instrumentId, LocalTime scheduledTime) {
        return new OrderDTO(
                quantity,
                QuotationDTO.fromBigDecimal(price),
                direction,
                accountId,
                OrderType.ORDER_TYPE_LIMIT,
                UUID.randomUUID().toString(),
                instrumentId,
                scheduledTime);
    }

    /**
     * Создает OrderDTO с указанным UUID.
     * 
     * @param quantity      количество лотов
     * @param price         цена в BigDecimal
     * @param direction     направление операции
     * @param accountId     идентификатор аккаунта
     * @param instrumentId  идентификатор инструмента
     * @param orderId       идентификатор заявки
     * @param scheduledTime время исполнения заявки
     * @return OrderDTO объект
     */
    public static OrderDTO createWithId(int quantity, java.math.BigDecimal price, OrderDirection direction,
            String accountId, String instrumentId, String orderId, LocalTime scheduledTime) {
        return new OrderDTO(
                quantity,
                QuotationDTO.fromBigDecimal(price),
                direction,
                accountId,
                OrderType.ORDER_TYPE_LIMIT,
                orderId,
                instrumentId,
                scheduledTime);
    }

    /**
     * Получает цену в виде BigDecimal.
     * 
     * @return цена как BigDecimal
     */
    public java.math.BigDecimal getPriceAsBigDecimal() {
        return price.toBigDecimal();
    }

    /**
     * Получает цену в виде BigDecimal с округлением до 2 знаков после запятой.
     * 
     * @return цена как BigDecimal с округлением до копеек
     */
    public java.math.BigDecimal getPriceAsBigDecimalRounded() {
        return price.toBigDecimal().setScale(6, java.math.RoundingMode.HALF_UP);
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
                && scheduledTime != null;
    }
}
