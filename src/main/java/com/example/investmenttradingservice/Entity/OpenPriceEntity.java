package com.example.investmenttradingservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "open_prices", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenPriceEntity {
    @EmbeddedId
    private OpenPriceKey id;

    @Column(name = "instrument_type", nullable = false)
    private String instrumentType;

    @Column(name = "open_price", nullable = false, precision = 18, scale = 9)
    private BigDecimal openPrice;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String exchange;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

    public OpenPriceEntity(LocalDate date, String figi, String instrumentType,
            BigDecimal openPrice, String currency, String exchange) {
        this.id = new OpenPriceKey(date, figi);
        this.instrumentType = instrumentType;
        this.openPrice = openPrice;
        this.currency = currency;
        this.exchange = exchange;
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
    }
}