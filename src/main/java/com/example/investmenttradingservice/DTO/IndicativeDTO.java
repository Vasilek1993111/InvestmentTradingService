package com.example.investmenttradingservice.DTO;

/**
 * DTO для индикативных инструментов (индексы, товары и другие)
 * Согласно документации Tinkoff Invest API:
 * https://developer.tbank.ru/invest/services/instruments/methods
 */
public record IndicativeDTO(
        String figi,
        String ticker,
        String name,
        String currency,
        String exchange,
        String classCode,
        String uid,
        Boolean sellAvailableFlag,
        Boolean buyAvailableFlag) {   
}
