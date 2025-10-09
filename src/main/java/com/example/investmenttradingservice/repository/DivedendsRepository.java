package com.example.investmenttradingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.investmenttradingservice.entity.DividendEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для работы с дивидендами
 * 
 * <p>
 * Предоставляет методы для получения данных о дивидендных событиях
 * из таблицы dividends. Основная задача - получение актуальных дивидендов
 * с учетом бизнес-логики по датам объявления и отсечкам.
 * </p>
 * 
 * <p>
 * Бизнес-логика загрузки дивидендов:
 * <ul>
 * <li>Обычные дни: declared_date между вчера и сегодня</li>
 * <li>Воскресенье: declared_date между позавчера и сегодня</li>
 * <li>Дивидендная отсечка: record_date = declared_date - 1</li>
 * <li>Цель: дивиденды на сегодня или завтра</li>
 * </ul>
 * </p>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public interface DivedendsRepository extends JpaRepository<DividendEntity, Long> {

    /**
     * Получает дивиденды на сегодня или завтра с учетом логики загрузки
     * 
     * <p>
     * Основная цель: получить дивиденды которые будут выплачены сегодня или завтра.
     * </p>
     * 
     * <p>
     * Логика загрузки declared_date:
     * </p>
     * <ul>
     * <li><strong>Обычные дни:</strong> declared_date между вчера и сегодня</li>
     * <li><strong>Воскресенье:</strong> declared_date между позавчера и
     * сегодня</li>
     * </ul>
     * 
     * <p>
     * <strong>Условия:</strong>
     * </p>
     * <ul>
     * <li>record_date = declared_date - 1 (дивидендная отсечка)</li>
     * <li>payment_date BETWEEN сегодня AND завтра (дивиденды на сегодня или
     * завтра)</li>
     * </ul>
     * 
     * @return список дивидендов на сегодня или завтра
     */
    @Query(value = """
            SELECT d.* FROM invest.dividends d
            WHERE (
                -- Обычные дни: declared_date между вчера и сегодня
                (EXTRACT(DOW FROM CURRENT_DATE) BETWEEN 1 AND 6
                 AND d.declared_date BETWEEN CURRENT_DATE - INTERVAL '1 day' AND CURRENT_DATE)
                OR
                -- Воскресенье: declared_date между позавчера и сегодня
                (EXTRACT(DOW FROM CURRENT_DATE) = 0
                 AND d.declared_date BETWEEN CURRENT_DATE - INTERVAL '2 days' AND CURRENT_DATE)
            )
            AND d.record_date = d.declared_date - INTERVAL '1 day'
            AND d.payment_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '1 day'
            ORDER BY d.payment_date ASC, d.figi
            """, nativeQuery = true)
    List<DividendEntity> findDividendsForTodayAndTomorrow();

    /**
     * Получает дивиденды по конкретному инструменту
     * 
     * <p>
     * Возвращает все дивиденды для указанного FIGI с учетом актуальности
     * (payment_date >= CURRENT_DATE).
     * </p>
     * 
     * @param figi уникальный идентификатор финансового инструмента
     * @return список дивидендов для указанного инструмента
     */
    @Query(value = """
            SELECT d.* FROM invest.dividends d
            WHERE d.figi = :figi
            AND d.payment_date >= CURRENT_DATE
            ORDER BY d.payment_date ASC
            """, nativeQuery = true)
    List<DividendEntity> findDividendsByFigi(@Param("figi") String figi);

    /**
     * Получает дивиденды по дате выплаты
     * 
     * <p>
     * Возвращает все дивиденды, которые будут выплачены в указанную дату.
     * </p>
     * 
     * @param paymentDate дата выплаты дивидендов
     * @return список дивидендов на указанную дату выплаты
     */
    @Query(value = """
            SELECT d.* FROM invest.dividends d
            WHERE d.payment_date = :paymentDate
            ORDER BY d.figi
            """, nativeQuery = true)
    List<DividendEntity> findDividendsByPaymentDate(@Param("paymentDate") LocalDate paymentDate);

    /**
     * Получает дивиденды по валюте
     * 
     * <p>
     * Возвращает актуальные дивиденды в указанной валюте.
     * </p>
     * 
     * @param currency валюта дивидендов (например, RUB, USD, EUR)
     * @return список дивидендов в указанной валюте
     */
    @Query(value = """
            SELECT d.* FROM invest.dividends d
            WHERE d.currency = :currency
            AND d.payment_date >= CURRENT_DATE
            ORDER BY d.payment_date ASC, d.figi
            """, nativeQuery = true)
    List<DividendEntity> findDividendsByCurrency(@Param("currency") String currency);

    /**
     * Получает дивиденды по типу
     * 
     * <p>
     * Возвращает актуальные дивиденды указанного типа.
     * </p>
     * 
     * @param dividendType тип дивидендов (например, "Regular", "Special")
     * @return список дивидендов указанного типа
     */
    @Query(value = """
            SELECT d.* FROM invest.dividends d
            WHERE d.dividend_type = :dividendType
            AND d.payment_date >= CURRENT_DATE
            ORDER BY d.payment_date ASC, d.figi
            """, nativeQuery = true)
    List<DividendEntity> findDividendsByType(@Param("dividendType") String dividendType);

    /**
     * Проверяет существование дивидендов для инструмента
     * 
     * <p>
     * Быстрая проверка наличия актуальных дивидендов для указанного FIGI.
     * </p>
     * 
     * @param figi уникальный идентификатор финансового инструмента
     * @return true если есть актуальные дивиденды, false в противном случае
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DividendEntity d WHERE d.figi = :figi AND d.paymentDate >= CURRENT_DATE")
    boolean existsActiveDividendsByFigi(@Param("figi") String figi);

    /**
     * Подсчитывает количество инструментов с актуальными дивидендами
     * 
     * <p>
     * Возвращает количество уникальных инструментов с дивидендами,
     * которые будут выплачены в будущем.
     * </p>
     * 
     * @return количество инструментов с актуальными дивидендами
     */
    @Query("SELECT COUNT(DISTINCT d.figi) FROM DividendEntity d WHERE d.paymentDate >= CURRENT_DATE")
    long countInstrumentsWithActiveDividends();

    /**
     * Получает статистику дивидендов
     * 
     * <p>
     * Возвращает агрегированную информацию о дивидендах:
     * - общее количество актуальных дивидендов
     * - общую сумму дивидендов
     * - количество уникальных инструментов
     * </p>
     * 
     * @return массив объектов: [количество, общая_сумма, количество_инструментов]
     */
    @Query(value = """
            SELECT
                COUNT(*) as total_count,
                COALESCE(SUM(dividend_value), 0) as total_amount,
                COUNT(DISTINCT figi) as instruments_count
            FROM invest.dividends
            WHERE payment_date >= CURRENT_DATE
            """, nativeQuery = true)
    Object[] getDividendsStatistics();
}