package com.example.investmenttradingservice.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;


    
public record ClosePriceDTO(
    LocalDate priceDate,
    String figi,
    String instrumentType,
    BigDecimal closePrice,
    String currency,
    String exchange,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
