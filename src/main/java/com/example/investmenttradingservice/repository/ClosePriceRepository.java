package com.example.investmenttradingservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.investmenttradingservice.entity.ClosePriceEntity;
import com.example.investmenttradingservice.entity.ClosePriceKey;

/**
 * Репозиторий для работы с ценами закрытия инструментов
 *
 * <p>
 * Предоставляет методы для поиска цен закрытия по различным критериям:
 * по дате, FIGI, типу инструмента и другим параметрам.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public interface ClosePriceRepository extends JpaRepository<ClosePriceEntity, ClosePriceKey> {

    /**
     * Находит все цены закрытия за указанную дату
     *
     * @param priceDate дата для поиска
     * @return список цен закрытия за дату
     */
    List<ClosePriceEntity> findById_PriceDate(LocalDate priceDate);

    /**
     * Проверяет существование цены закрытия для указанной даты и FIGI
     *
     * @param priceDate дата цены
     * @param figi      FIGI инструмента
     * @return true, если цена существует
     */
    boolean existsById_PriceDateAndId_Figi(LocalDate priceDate, String figi);
}