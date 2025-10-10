package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;

/**
 * DTO для котировки - денежная сумма без указания валюты.
 * Представляет цену в виде целой части (units) и дробной части (nano).
 * 
 * @param units целая часть суммы, может быть отрицательным числом
 * @param nano  дробная часть суммы, может быть отрицательным числом
 */
public record QuotationDTO(
        long units,
        int nano) {

    /**
     * Создает QuotationDTO из BigDecimal цены.
     * 
     * @param price цена в виде BigDecimal
     * @return QuotationDTO объект
     */
    public static QuotationDTO fromBigDecimal(BigDecimal price) {
        if (price == null) {
            return new QuotationDTO(0L, 0);
        }

        // Получаем целую часть
        long units = price.longValue();

        // Получаем дробную часть (nano = 9 знаков после запятой)
        BigDecimal fractionalPart = price.subtract(BigDecimal.valueOf(units));
        BigDecimal nanoDecimal = fractionalPart.multiply(BigDecimal.valueOf(1_000_000_000L));
        int nano = nanoDecimal.intValue();

        return new QuotationDTO(units, nano);
    }

    /**
     * Преобразует QuotationDTO обратно в BigDecimal.
     * 
     * @return BigDecimal цена
     */
    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(units)
                .add(BigDecimal.valueOf(nano).divide(BigDecimal.valueOf(1_000_000_000L), 9,
                        java.math.RoundingMode.HALF_UP));
    }

    /**
     * Проверяет, является ли котировка нулевой.
     * 
     * @return true если units и nano равны нулю
     */
    public boolean isZero() {
        return units == 0L && nano == 0;
    }

    /**
     * Проверяет, является ли котировка положительной.
     * 
     * @return true если котировка больше нуля
     */
    public boolean isPositive() {
        return units > 0L || (units == 0L && nano > 0);
    }
}