package com.example.investmenttradingservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.investmenttradingservice.entity.ClosePriceEveningSessionEntity;
import com.example.investmenttradingservice.entity.ClosePriceEveningSessionKey;

public interface ClosePriceEveningSessionRepository
        extends JpaRepository<ClosePriceEveningSessionEntity, ClosePriceEveningSessionKey> {
    List<ClosePriceEveningSessionEntity> findByPriceDate(LocalDate priceDate);
}
