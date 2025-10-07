package com.example.investmenttradingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.investmenttradingservice.Entity.ShareEntity;
import java.util.List;

/**
 * Репозиторий для работы с акциями в базе данных
 * 
 * <p>
 * Этот интерфейс предоставляет методы для выполнения операций CRUD с сущностями
 * ShareEntity.
 * Наследует функциональность JpaRepository, что обеспечивает базовые операции с
 * базой данных.
 * </p>
 * 
 * <p>
 * Основные возможности:
 * </p>
 * <ul>
 * <li>Сохранение и обновление акций</li>
 * <li>Поиск акций по различным критериям</li>
 * <li>Получение списков акций с фильтрацией</li>
 * <li>Удаление акций</li>
 * </ul>
 * 
 * <p>
 * Примеры использования:
 * </p>
 * 
 * <pre>{@code
 * // Сохранение акции
 * shareRepository.save(shareEntity);
 * 
 * // Поиск по FIGI
 * Optional<ShareEntity> share = shareRepository.findById("BBG004730N88");
 * 
 * // Поиск по тикеру
 * List<ShareEntity> shares = shareRepository.findByTicker("SBER");
 * }</pre>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface ShareRepository extends JpaRepository<ShareEntity, String> {

    /**
     * Находит акции по торговому символу (тикеру)
     * 
     * <p>
     * Выполняет поиск всех акций с указанным тикером.
     * Поиск выполняется без учета регистра.
     * </p>
     * 
     * @param ticker торговый символ для поиска (например, "SBER", "GAZP")
     * @return список найденных акций
     */
    List<ShareEntity> findByTicker(String ticker);

    /**
     * Находит акции по бирже
     * 
     * <p>
     * Возвращает все акции, торгуемые на указанной бирже.
     * </p>
     * 
     * @param exchange название биржи (например, "MOEX", "NYSE")
     * @return список акций, торгуемых на указанной бирже
     */
    List<ShareEntity> findByExchange(String exchange);

    /**
     * Находит акции по сектору экономики
     * 
     * <p>
     * Возвращает все акции из указанного сектора экономики.
     * </p>
     * 
     * @param sector сектор экономики (например, "Финансы", "Энергетика")
     * @return список акций из указанного сектора
     */
    List<ShareEntity> findBySector(String sector);

    /**
     * Находит акции по валюте торговли
     * 
     * <p>
     * Возвращает все акции, торгуемые в указанной валюте.
     * </p>
     * 
     * @param currency валюта торговли (например, "RUB", "USD", "EUR")
     * @return список акций, торгуемых в указанной валюте
     */
    List<ShareEntity> findByCurrency(String currency);

    /**
     * Находит акции по статусу торговли
     * 
     * <p>
     * Возвращает все акции с указанным статусом торговли.
     * </p>
     * 
     * @param tradingStatus статус торговли (например, "NormalTrading",
     *                      "NotAvailableForTrading")
     * @return список акций с указанным статусом торговли
     */
    List<ShareEntity> findByTradingStatus(String tradingStatus);

    /**
     * Находит акции, для которых разрешены короткие продажи
     * 
     * <p>
     * Возвращает все акции, для которых установлен флаг shortEnabled = true.
     * </p>
     * 
     * @return список акций с разрешенными короткими продажами
     */
    List<ShareEntity> findByShortEnabledTrue();

    /**
     * Находит акции по частичному совпадению названия
     * 
     * <p>
     * Выполняет поиск акций по частичному совпадению названия компании.
     * Поиск выполняется без учета регистра.
     * </p>
     * 
     * @param namePart часть названия для поиска
     * @return список найденных акций
     */
    @Query("SELECT s FROM ShareEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<ShareEntity> findByNameContainingIgnoreCase(@Param("namePart") String namePart);

    /**
     * Подсчитывает количество акций на указанной бирже
     * 
     * <p>
     * Возвращает общее количество акций, торгуемых на указанной бирже.
     * </p>
     * 
     * @param exchange название биржи
     * @return количество акций на бирже
     */
    long countByExchange(String exchange);

    /**
     * Проверяет существование акции по FIGI
     * 
     * <p>
     * Возвращает true, если акция с указанным FIGI существует в базе данных.
     * </p>
     * 
     * @param figi уникальный идентификатор инструмента
     * @return true если акция существует, false в противном случае
     */
    boolean existsByFigi(String figi);

}
