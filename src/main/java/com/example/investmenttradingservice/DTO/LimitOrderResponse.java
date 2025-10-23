package com.example.investmenttradingservice.DTO;

import java.util.List;

/**
 * DTO для ответа на создание лимитных ордеров.
 * Содержит информацию о созданных ордерах и метаданные операции.
 * 
 * @param orders           список созданных ордеров
 * @param totalOrders      общее количество созданных ордеров
 * @param limitType        тип лимита (limitUp/limitDown)
 * @param instrumentsCount количество обработанных инструментов
 * 
 * @author Investment Trading Service
 * @version 1.0
 */
public record LimitOrderResponse(
        List<OrderDTO> orders,
        int totalOrders,
        String limitType,
        int instrumentsCount) {

    /**
     * Создает ответ на основе списка ордеров и типа лимита.
     * 
     * @param orders           список созданных ордеров
     * @param limitType        тип лимита
     * @param instrumentsCount количество инструментов
     * @return новый экземпляр LimitOrderResponse
     */
    public static LimitOrderResponse of(List<OrderDTO> orders, String limitType, int instrumentsCount) {
        return new LimitOrderResponse(
                orders,
                orders != null ? orders.size() : 0,
                limitType,
                instrumentsCount);
    }

    /**
     * Проверяет, были ли созданы ордера.
     * 
     * @return true если ордера созданы, false иначе
     */
    public boolean hasOrders() {
        return orders != null && !orders.isEmpty();
    }

    /**
     * Получает количество успешно созданных ордеров.
     * 
     * @return количество ордеров
     */
    public int getSuccessCount() {
        return totalOrders;
    }
}
