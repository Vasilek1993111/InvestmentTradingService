package com.example.investmenttradingservice.Entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ClosePriceKey implements Serializable {
    private LocalDate priceDate;
    private String figi;

    // Конструкторы
    public ClosePriceKey() {
    }

    public ClosePriceKey(LocalDate priceDate, String figi) {
        this.priceDate = priceDate;
        this.figi = figi;
    }

    // Геттеры и сеттеры
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

    // Реализация equals и hashCode (обязательно для композитных ключей)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClosePriceKey that = (ClosePriceKey) o;
        return Objects.equals(priceDate, that.priceDate) && Objects.equals(figi, that.figi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceDate, figi);
    }
}
