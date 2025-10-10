package com.example.investmenttradingservice.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO для создания отложенных заявок.
 * Содержит валидацию направления торговли и времени исполнения.
 * 
 * @param instruments Список инструментов для торговли
 * @param price       Цена заявки
 * @param direction   Направление торговли (buy, sell, all)
 * @param start_time  Время начала исполнения в формате HH:mm:ss
 */
public record GroupOrderRequest(
        @NotEmpty(message = "Список инструментов не может быть пустым") 
        List<String> instruments,

        @NotBlank(message = "Опорная цена не может быть пустой") 
        String main_price,

        
        @NotBlank(message = "Сумма не может быть пустой") 
        BigDecimal amount,

        @NotBlank(message = "Направление торговли не может быть пустым") @Pattern(regexp = "^(buy|sell|all)$", message = "Направление торговли должно быть: buy, sell или all")
        String direction,

        @NotNull(message = "Время начала не может быть null") @JsonFormat(pattern = "HH:mm:ss") 
        LocalTime start_time,

        @NotBlank(message = "Должен быть хотя бы один уровень")
        LevelsDTO levels
        ) 
        
        {

    /**
     * Проверяет, является ли направление покупкой.
     * 
     * @return true если direction равен "buy"
     */
    public boolean isBuy() {
        return "buy".equals(direction);
    }

    /**
     * Проверяет, является ли направление продажей.
     * 
     * @return true если direction равен "sell"
     */
    public boolean isSell() {
        return "sell".equals(direction);
    }

    /**
     * Проверяет, является ли направление "все" (и покупка, и продажа).
     * 
     * @return true если direction равен "all"
     */
    public boolean isAll() {
        return "all".equals(direction);
    }
}
