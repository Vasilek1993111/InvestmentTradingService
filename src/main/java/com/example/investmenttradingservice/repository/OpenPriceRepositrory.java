package com.example.investmenttradingservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.investmenttradingservice.entity.OpenPriceEntity;
import com.example.investmenttradingservice.entity.OpenPriceKey;

public interface OpenPriceRepositrory extends JpaRepository<OpenPriceEntity, OpenPriceKey> {

    /**
     * Находит все цены закрытия за указанную дату
     *
     * @param priceDate дата для поиска
     * @return список цен закрытия за дату
     */
    List<OpenPriceEntity> findById_PriceDate(LocalDate priceDate);

}
