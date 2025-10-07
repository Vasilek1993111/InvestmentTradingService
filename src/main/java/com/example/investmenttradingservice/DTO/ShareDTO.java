package com.example.investmenttradingservice.DTO;

/**
 * Data Transfer Object (DTO) для передачи данных об акциях
 * 
 * <p>
 * Этот record представляет акцию в формате, удобном для передачи между слоями
 * приложения.
 * Используется для API ответов и передачи данных между сервисами.
 * </p>
 * 
 * <p>
 * Основные поля:
 * </p>
 * <ul>
 * <li>figi - уникальный идентификатор инструмента</li>
 * <li>ticker - торговый символ акции</li>
 * <li>name - полное название компании</li>
 * <li>currency - валюта торговли</li>
 * <li>exchange - биржа, где торгуется акция</li>
 * <li>sector - сектор экономики</li>
 * <li>tradingStatus - статус торговли</li>
 * <li>shortEnabled - возможность коротких продаж</li>
 * <li>assetUid - уникальный идентификатор актива</li>
 * </ul>
 * 
 * <p>
 * Пример использования:
 * </p>
 * 
 * <pre>{@code
 * ShareDTO share = new ShareDTO(
 *         "BBG004730N88", "SBER", "Сбербанк", "RUB",
 *         "MOEX", "Финансы", "NormalTrading", true, "asset123");
 * }</pre>
 * 
 * @param figi          уникальный идентификатор финансового инструмента (FIGI)
 * @param ticker        торговый символ акции (например, SBER, GAZP)
 * @param name          полное название компании-эмитента
 * @param currency      валюта, в которой торгуется акция (RUB, USD, EUR)
 * @param exchange      биржа, где торгуется акция (MOEX, NYSE, NASDAQ)
 * @param sector        сектор экономики (Финансы, Энергетика, IT и т.д.)
 * @param tradingStatus статус торговли (NormalTrading, NotAvailableForTrading и
 *                      т.д.)
 * @param shortEnabled  возможность совершения коротких продаж
 * @param assetUid      уникальный идентификатор актива
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public record ShareDTO(
        String figi,
        String ticker,
        String name,
        String currency,
        String exchange,
        String sector,
        String tradingStatus,
        Boolean shortEnabled,
        String assetUid) {

}
