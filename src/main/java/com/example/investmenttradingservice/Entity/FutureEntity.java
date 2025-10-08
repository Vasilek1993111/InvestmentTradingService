package com.example.investmenttradingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.example.investmenttradingservice.util.TimeZoneUtils;

/**
 * Сущность для представления фьючерсов в базе данных
 * 
 * <p>
 * Этот класс представляет фьючерсный контракт как финансовый инструмент в
 * системе.
 * Фьючерс - это производный финансовый инструмент, который обязывает стороны
 * купить или продать актив по определенной цене в будущем.
 * </p>
 * 
 * <p>
 * Основные поля:
 * </p>
 * <ul>
 * <li>figi - уникальный идентификатор инструмента (первичный ключ)</li>
 * <li>ticker - торговый символ фьючерса</li>
 * <li>assetType - тип базового актива</li>
 * <li>basicAsset - базовый актив фьючерса</li>
 * <li>currency - валюта торговли</li>
 * <li>exchange - биржа, где торгуется фьючерс</li>
 * <li>expirationDate - дата экспирации контракта</li>
 * </ul>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Entity
@Table(name = "futures", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FutureEntity {

    /** Уникальный идентификатор финансового инструмента (FIGI) */
    @Id
    private String figi;

    /** Торговый символ фьючерса (например, Si-3.24, RTS-3.24) */
    @Column(name = "ticker", nullable = false)
    private String ticker;

    /** Тип базового актива (Share, Currency, Commodity и т.д.) */
    @Column(name = "asset_type", nullable = false)
    private String assetType;

    /** Базовый актив фьючерса (например, USD/RUB, SBER) */
    @Column(name = "basic_asset", nullable = false)
    private String basicAsset;

    /** Валюта, в которой торгуется фьючерс (RUB, USD, EUR) */
    @Column(name = "currency", nullable = false)
    private String currency;

    /** Биржа, где торгуется фьючерс (MOEX, CME и т.д.) */
    @Column(name = "exchange", nullable = false)
    private String exchange;

    /** Возможность совершения коротких продаж */
    @Column(name = "short_enabled")
    private Boolean shortEnabled;

    /** Дата и время экспирации фьючерсного контракта */
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    /** Дата и время создания записи (автоматически устанавливается) */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());

    /** Дата и время последнего обновления записи (автоматически обновляется) */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());

    /**
     * Конструктор для создания нового фьючерса
     * 
     * <p>
     * Создает новый экземпляр FutureEntity с указанными параметрами.
     * Временные метки createdAt и updatedAt устанавливаются автоматически.
     * </p>
     * 
     * @param figi           уникальный идентификатор инструмента
     * @param ticker         торговый символ фьючерса
     * @param assetType      тип базового актива
     * @param basicAsset     базовый актив фьючерса
     * @param currency       валюта торговли
     * @param exchange       биржа торговли
     * @param shortEnabled   возможность коротких продаж
     * @param expirationDate дата экспирации контракта
     */
    public FutureEntity(String figi, String ticker, String assetType, String basicAsset,
            String currency, String exchange, Boolean shortEnabled, LocalDateTime expirationDate) {
        this.figi = figi;
        this.ticker = ticker;
        this.assetType = assetType;
        this.basicAsset = basicAsset;
        this.currency = currency;
        this.exchange = exchange;
        this.shortEnabled = shortEnabled;
        this.expirationDate = expirationDate;
        this.createdAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
        this.updatedAt = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
    }

}
