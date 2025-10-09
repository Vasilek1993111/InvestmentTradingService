package com.example.investmenttradingservice.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;


public record DividendDto(
    String figi,

    @JsonFormat(pattern = "yyyy-MM-dd") 
    LocalDate declaredDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate recordDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate paymentDate,

    BigDecimal dividendValue,

    String currency,

    String dividendType
) {}
