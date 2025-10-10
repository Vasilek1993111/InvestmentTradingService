package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record LevelsDTO(
        @NotNull @DecimalMin(value = "0.00", message = "Процент не может быть отрицательным") @DecimalMax(value = "100.00", message = "Процент не может превышать 100%") @Digits(integer = 3, fraction = 4, message = "Процент должен иметь максимум 3 цифры до запятой и 4 после") BigDecimal level1,

        @DecimalMin(value = "0.00", message = "Процент не может быть отрицательным") @DecimalMax(value = "100.00", message = "Процент не может превышать 100%") @Digits(integer = 3, fraction = 4, message = "Процент должен иметь максимум 3 цифры до запятой и 4 после") BigDecimal level2,

        @DecimalMin(value = "0.00", message = "Процент не может быть отрицательным") @DecimalMax(value = "100.00", message = "Процент не может превышать 100%") @Digits(integer = 3, fraction = 4, message = "Процент должен иметь максимум 3 цифры до запятой и 4 после") BigDecimal level3) {
    /**
     * Преобразует процент в десятичную дробь (например, 15.5% -> 0.155)
     * 
     * @param level процентное значение
     * @return десятичная дробь
     */
    public BigDecimal toDecimal(BigDecimal level) {
        return level.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
    }

    /**
     * Создает процент из десятичной дроби (например, 0.155 -> 15.5%)
     * 
     * @param decimal десятичная дробь
     * @return процентное значение
     */
    public static BigDecimal fromDecimal(BigDecimal decimal) {
        return decimal.multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Подсчитывает количество заполненных уровней в текущем запросе.
     * level1 всегда обязателен, level2 и level3 могут быть null.
     * 
     * @return количество заполненных уровней (от 1 до 3)
     */
    public int getLevelsCount() {
        int count = 1; // level1 всегда присутствует (обязательное поле)

        if (level2 != null) {
            count++;
        }

        if (level3 != null) {
            count++;
        }

        return count;
    }

    /**
     * Проверяет, заполнен ли указанный уровень.
     * 
     * @param levelNumber номер уровня (1, 2 или 3)
     * @return true если уровень заполнен, false если null
     */
    public boolean isLevelFilled(int levelNumber) {
        return switch (levelNumber) {
            case 1 -> level1 != null;
            case 2 -> level2 != null;
            case 3 -> level3 != null;
            default -> false;
        };
    }

    /**
     * Получает значение указанного уровня.
     * 
     * @param levelNumber номер уровня (1, 2 или 3)
     * @return значение уровня или null если уровень не заполнен
     */
    public BigDecimal getLevelValue(int levelNumber) {
        return switch (levelNumber) {
            case 1 -> level1;
            case 2 -> level2;
            case 3 -> level3;
            default -> null;
        };
    }

}
