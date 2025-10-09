package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;
import java.time.Instant;

public record LastPriceDTO(String figi,
    String direction,
    BigDecimal price,
    long quantity,
    Instant time,
    String tradeSource
) {}
    
