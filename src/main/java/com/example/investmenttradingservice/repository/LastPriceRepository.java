package com.example.investmenttradingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.investmenttradingservice.entity.LastPriceEntity;
import com.example.investmenttradingservice.entity.LastPriceKey;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с ценами последних сделок
 * 
 * <p>
 * Предоставляет методы для получения данных о последних ценах торговых
 * инструментов
 * из таблицы last_prices. Основная задача - получение актуальной цены для
 * каждого инструмента.
 * </p>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public interface LastPriceRepository extends JpaRepository<LastPriceEntity, LastPriceKey> {

    /**
     * Получает последнюю цену для конкретного инструмента по FIGI
     * 
     * <p>
     * Возвращает запись с самой поздней временной меткой для указанного FIGI.
     * Используется для получения актуальной цены конкретного инструмента.
     * </p>
     * 
     * @param figi уникальный идентификатор финансового инструмента
     * @return Optional с последней ценой или пустой Optional если данные не найдены
     */
    @Query("SELECT lp FROM LastPriceEntity lp WHERE lp.id.figi = :figi ORDER BY lp.id.time DESC LIMIT 1")
    Optional<LastPriceEntity> findLatestPriceByFigi(@Param("figi") String figi);

    /**
     * Получает все последние цены по каждому инструменту
     * 
     * <p>
     * Выполняет группировку по FIGI и возвращает запись с максимальным временем
     * для каждого инструмента. Эффективно получает актуальные цены всех
     * инструментов
     * одним запросом.
     * </p>
     * 
     * <p>
     * SQL запрос использует оконную функцию ROW_NUMBER() для оптимизации:
     * - Группирует по figi
     * - Сортирует по времени DESC (самые новые записи первыми)
     * - Выбирает только первую запись из каждой группы (последнюю по времени)
     * </p>
     * 
     * @return список LastPriceEntity с последними ценами для каждого инструмента
     */
    @Query(value = """
            SELECT lp.* FROM (
                SELECT *,
                       ROW_NUMBER() OVER (PARTITION BY figi ORDER BY time DESC) as rn
                FROM invest.last_prices
            ) lp
            WHERE lp.rn = 1
            ORDER BY lp.figi
            """, nativeQuery = true)
    List<LastPriceEntity> findAllLatestPrices();

    /**
     * Проверяет существование последней цены для инструмента
     * 
     * <p>
     * Быстрая проверка наличия данных о последней цене для указанного FIGI.
     * Используется для валидации перед выполнением операций.
     * </p>
     * 
     * @param figi уникальный идентификатор финансового инструмента
     * @return true если последняя цена существует, false в противном случае
     */
    @Query("SELECT CASE WHEN COUNT(lp) > 0 THEN true ELSE false END FROM LastPriceEntity lp WHERE lp.id.figi = :figi")
    boolean existsLatestPriceByFigi(@Param("figi") String figi);

    /**
     * Подсчитывает количество инструментов с последними ценами
     * 
     * <p>
     * Возвращает количество уникальных инструментов, для которых есть данные
     * о последних ценах. Полезно для мониторинга покрытия данных.
     * </p>
     * 
     * @return количество инструментов с последними ценами
     */
    @Query("SELECT COUNT(DISTINCT lp.id.figi) FROM LastPriceEntity lp")
    long countDistinctInstruments();
}
