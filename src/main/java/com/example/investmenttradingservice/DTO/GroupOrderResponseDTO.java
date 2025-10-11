package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO для ответа при создании групповых заявок.
 * Содержит список созданных заявок и цену инструмента из поля main_price.
 * 
 * @param orders          список созданных заявок
 * @param instrumentPrice цена инструмента из main_price поля
 */
public record GroupOrderResponseDTO(
        List<OrderDTO> orders,
        BigDecimal instrumentPrice) {

    /**
     * Создает ответ с пустым списком заявок и нулевой ценой.
     * 
     * @return пустой ответ
     */
    public static GroupOrderResponseDTO empty() {
        return new GroupOrderResponseDTO(List.of(), BigDecimal.ZERO);
    }

    /**
     * Создает ответ с заявками и ценой инструмента.
     * 
     * @param orders          список заявок
     * @param instrumentPrice цена инструмента
     * @return ответ с заявками и ценой
     */
    public static GroupOrderResponseDTO of(List<OrderDTO> orders, BigDecimal instrumentPrice) {
        return new GroupOrderResponseDTO(orders, instrumentPrice);
    }
}
