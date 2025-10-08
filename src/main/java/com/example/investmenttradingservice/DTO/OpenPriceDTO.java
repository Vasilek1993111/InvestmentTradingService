package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OpenPriceDTO(
    String figi,
    LocalDate priceDate,
    BigDecimal openPrice,
    String instrumentType,
    String currency,
    String exchange,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}