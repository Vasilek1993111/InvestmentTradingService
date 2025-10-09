package com.example.investmenttradingservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "dividends", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DividendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "figi", nullable = false)
    private String figi;

    @Column(name = "declared_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate declaredDate;

    @Column(name = "record_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    @Column(name = "payment_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @Column(name = "dividend_value", precision = 18, scale = 9)
    private BigDecimal dividendValue;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "dividend_type", length = 50)
    private String dividendType;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

    public DividendEntity(String figi, LocalDate declaredDate, LocalDate recordDate,
            LocalDate paymentDate, BigDecimal dividendValue,
            String currency, String dividendType) {
        this.figi = figi;
        this.declaredDate = declaredDate;
        this.recordDate = recordDate;
        this.paymentDate = paymentDate;
        this.dividendValue = dividendValue;
        this.currency = currency;
        this.dividendType = dividendType;
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
    }
}
