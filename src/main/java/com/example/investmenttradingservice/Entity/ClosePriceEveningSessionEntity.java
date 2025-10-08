package com.example.investmenttradingservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "close_prices_evening_session", schema = "invest")
@IdClass(ClosePriceEveningSessionKey.class)
public class ClosePriceEveningSessionEntity {

    @Id
    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Id
    @Column(name = "figi", nullable = false)
    private String figi;

    @Column(name = "close_price", nullable = false, precision = 18, scale = 9)
    private BigDecimal closePrice;

    @Column(name = "instrument_type")
    private String instrumentType;

    @Column(name = "currency")
    private String currency;

    @Column(name = "exchange")
    private String exchange;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ClosePriceEveningSessionEntity() {
    }

    public ClosePriceEveningSessionEntity(LocalDate priceDate, String figi, BigDecimal closePrice,
            String instrumentType, String currency, String exchange) {
        this.priceDate = priceDate;
        this.figi = figi;
        this.closePrice = closePrice;
        this.instrumentType = instrumentType;
        this.currency = currency;
        this.exchange = exchange;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }

    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}   