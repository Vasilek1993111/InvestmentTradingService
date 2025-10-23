package com.example.investmenttradingservice.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO для уровней лимитных ордеров.
 * Поддерживает два типа лимитов: limitUp и limitDown.
 * 
 * @param level  тип лимита - "limitUp" или "limitDown"
 * @param level2 дополнительный уровень (опциональный)
 * 
 * @author Investment Trading Service
 * @version 1.0
 */
public record LevelLimitDto(
        @NotNull(message = "Тип лимита обязателен") @Pattern(regexp = "^(limitUp|limitDown)$", message = "Тип лимита должен быть 'limitUp' или 'limitDown'") String level,
        String level2) {

    /**
     * Проверяет, является ли лимит восходящим (limitUp).
     * 
     * @return true если лимит восходящий, false иначе
     */
    public boolean isLimitUp() {
        return "limitUp".equals(level);
    }

    /**
     * Проверяет, является ли лимит нисходящим (limitDown).
     * 
     * @return true если лимит нисходящий, false иначе
     */
    public boolean isLimitDown() {
        return "limitDown".equals(level);
    }
}
