package com.example.investmenttradingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shares", schema = "invest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareEntity {
    @Id
    private String figi;
    private String ticker;
    private String name;
    private String currency;
    private String exchange;
    private String sector;
    private String tradingStatus;
    private Boolean shortEnabled;
    private String assetUid;
    /** Минимальный шаг цены */
    @Column(name = "min_price_increment")
    private BigDecimal minPriceIncrement;

    /** Размер лота инструмента */
    @Column(name = "lot")
    private Integer lot;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}