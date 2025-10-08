package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClosePriceEveningSessionDTO(
        LocalDate priceDate,
        String figi,
        BigDecimal closePrice,
        String instrumentType,
        String currency,
        String exchange) {
}