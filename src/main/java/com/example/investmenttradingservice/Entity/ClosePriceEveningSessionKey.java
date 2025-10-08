package com.example.investmenttradingservice.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class ClosePriceEveningSessionKey implements Serializable {

    private LocalDate priceDate;
    private String figi;

    public ClosePriceEveningSessionKey() {
    }

    public ClosePriceEveningSessionKey(LocalDate priceDate, String figi) {
        this.priceDate = priceDate;
        this.figi = figi;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClosePriceEveningSessionKey that = (ClosePriceEveningSessionKey) o;
        return Objects.equals(priceDate, that.priceDate) && Objects.equals(figi, that.figi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceDate, figi);
    }
}
