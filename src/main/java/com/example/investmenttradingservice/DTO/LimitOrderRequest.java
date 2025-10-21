package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * DTO для создания лимитных ордеров.
 * Поддерживает создание ордеров с лимитами limitUp/limitDown для множественных
 * инструментов.
 * 
 * @param instruments список идентификаторов инструментов (FIGI)
 * @param amount      сумма для торговли
 * @param direction   направление торговли ("buy" или "sell")
 * @param startTime   время начала торговли ("now" или конкретное время
 *                    HH:mm:ss)
 * @param levels      настройки лимитов
 * 
 * @author Investment Trading Service
 * @version 1.0
 */
public record LimitOrderRequest(
        @NotEmpty(message = "Список инструментов не может быть пустым") List<@NotBlank(message = "Идентификатор инструмента не может быть пустым") String> instruments,

        @NotNull(message = "Сумма обязательна") @Positive(message = "Сумма должна быть положительной") BigDecimal amount,

        @NotBlank(message = "Направление торговли обязательно") @Pattern(regexp = "^(buy|sell)$", message = "Направление должно быть 'buy' или 'sell'") String direction,

        @NotNull(message = "Время начала не может быть null") @JsonFormat(pattern = "HH:mm:ss") @JsonDeserialize(using = com.example.investmenttradingservice.util.LocalTimeOrNowDeserializer.class) LocalTime start_time,

        @NotNull(message = "Уровни лимитов обязательны") @Valid LevelLimitDto levels) {

    /**
     * Проверяет, является ли время начала "сейчас".
     * 
     * @return true если start_time равно "now", false иначе
     */
    public boolean isStartNow() {
        return start_time == null || "now".equals(start_time.toString());
    }

    /**
     * Проверяет, является ли направление покупкой.
     * 
     * @return true если direction равно "buy", false иначе
     */
    public boolean isBuyDirection() {
        return "buy".equals(direction);
    }

    /**
     * Проверяет, является ли направление продажей.
     * 
     * @return true если direction равно "sell", false иначе
     */
    public boolean isSellDirection() {
        return "sell".equals(direction);
    }

    /**
     * Получает количество инструментов в запросе.
     * 
     * @return количество инструментов
     */
    public int getInstrumentsCount() {
        return instruments != null ? instruments.size() : 0;
    }
}
