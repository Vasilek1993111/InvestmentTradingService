package com.example.investmenttradingservice.DTO;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) для передачи данных о фьючерсах
 * 
 * <p>
 * Этот record представляет фьючерсный контракт в формате, удобном для передачи
 * между слоями приложения.
 * Используется для API ответов и передачи данных между сервисами.
 * </p>
 * 
 * <p>
 * Основные поля:
 * </p>
 * <ul>
 * <li>figi - уникальный идентификатор инструмента</li>
 * <li>ticker - торговый символ фьючерса</li>
 * <li>assetType - тип базового актива</li>
 * <li>basicAsset - базовый актив фьючерса</li>
 * <li>currency - валюта торговли</li>
 * <li>exchange - биржа, где торгуется фьючерс</li>
 * <li>shortEnabled - возможность коротких продаж</li>
 * <li>expirationDate - дата экспирации контракта</li>
 * </ul>
 * 
 * <p>
 * Пример использования:
 * </p>
 * 
 * <pre>{@code
 * FutureDTO future = new FutureDTO(
 *         "FUTSI0324000", "Si-3.24", "Currency", "USD/RUB",
 *         "RUB", "MOEX", true, LocalDateTime.of(2024, 3, 15, 0, 0));
 * }</pre>
 * 
 * @param figi           уникальный идентификатор финансового инструмента (FIGI)
 * @param ticker         торговый символ фьючерса (например, Si-3.24, RTS-3.24)
 * @param assetType      тип базового актива (Share, Currency, Commodity и т.д.)
 * @param basicAsset     базовый актив фьючерса (например, USD/RUB, SBER)
 * @param currency       валюта, в которой торгуется фьючерс (RUB, USD, EUR)
 * @param exchange       биржа, где торгуется фьючерс (MOEX, CME и т.д.)
 * @param shortEnabled   возможность совершения коротких продаж
 * @param expirationDate дата и время экспирации фьючерсного контракта
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public record FutureDTO(
        String figi,
        String ticker,
        String assetType,
        String basicAsset,
        String currency,
        String exchange,
        Boolean shortEnabled,
        LocalDateTime expirationDate

) {
}
