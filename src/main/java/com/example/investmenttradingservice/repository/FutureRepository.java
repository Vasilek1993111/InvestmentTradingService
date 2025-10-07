package com.example.investmenttradingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.investmenttradingservice.Entity.FutureEntity;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с фьючерсами в базе данных
 * 
 * <p>
 * Этот интерфейс предоставляет методы для выполнения операций CRUD с сущностями
 * FutureEntity.
 * Наследует функциональность JpaRepository, что обеспечивает базовые операции с
 * базой данных.
 * </p>
 * 
 * <p>
 * Основные возможности:
 * </p>
 * <ul>
 * <li>Сохранение и обновление фьючерсов</li>
 * <li>Поиск фьючерсов по различным критериям</li>
 * <li>Получение списков фьючерсов с фильтрацией</li>
 * <li>Работа с датами экспирации</li>
 * </ul>
 * 
 * <p>
 * Примеры использования:
 * </p>
 * 
 * <pre>{@code
 * // Сохранение фьючерса
 * futureRepository.save(futureEntity);
 * 
 * // Поиск по FIGI
 * Optional<FutureEntity> future = futureRepository.findById("FUTSI0324000");
 * 
 * // Поиск по тикеру
 * List<FutureEntity> futures = futureRepository.findByTicker("Si-3.24");
 * }</pre>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Repository
public interface FutureRepository extends JpaRepository<FutureEntity, String> {

    /**
     * Находит фьючерсы по торговому символу (тикеру)
     * 
     * <p>
     * Выполняет поиск всех фьючерсов с указанным тикером.
     * </p>
     * 
     * @param ticker торговый символ для поиска (например, "Si-3.24", "RTS-3.24")
     * @return список найденных фьючерсов
     */
    List<FutureEntity> findByTicker(String ticker);

    /**
     * Находит фьючерсы по типу базового актива
     * 
     * <p>
     * Возвращает все фьючерсы с указанным типом базового актива.
     * </p>
     * 
     * @param assetType тип базового актива (например, "Currency", "Share",
     *                  "Commodity")
     * @return список фьючерсов с указанным типом актива
     */
    List<FutureEntity> findByAssetType(String assetType);

    /**
     * Находит фьючерсы по базовому активу
     * 
     * <p>
     * Возвращает все фьючерсы, основанные на указанном базовом активе.
     * </p>
     * 
     * @param basicAsset базовый актив (например, "USD/RUB", "SBER")
     * @return список фьючерсов с указанным базовым активом
     */
    List<FutureEntity> findByBasicAsset(String basicAsset);

    /**
     * Находит фьючерсы по бирже
     * 
     * <p>
     * Возвращает все фьючерсы, торгуемые на указанной бирже.
     * </p>
     * 
     * @param exchange название биржи (например, "MOEX", "CME")
     * @return список фьючерсов, торгуемых на указанной бирже
     */
    List<FutureEntity> findByExchange(String exchange);

    /**
     * Находит фьючерсы по валюте торговли
     * 
     * <p>
     * Возвращает все фьючерсы, торгуемые в указанной валюте.
     * </p>
     * 
     * @param currency валюта торговли (например, "RUB", "USD", "EUR")
     * @return список фьючерсов, торгуемых в указанной валюте
     */
    List<FutureEntity> findByCurrency(String currency);

    /**
     * Находит фьючерсы, для которых разрешены короткие продажи
     * 
     * <p>
     * Возвращает все фьючерсы, для которых установлен флаг shortEnabled = true.
     * </p>
     * 
     * @return список фьючерсов с разрешенными короткими продажами
     */
    List<FutureEntity> findByShortEnabledTrue();

    /**
     * Находит фьючерсы, истекающие до указанной даты
     * 
     * <p>
     * Возвращает все фьючерсы, дата экспирации которых меньше указанной даты.
     * </p>
     * 
     * @param expirationDate максимальная дата экспирации
     * @return список фьючерсов, истекающих до указанной даты
     */
    List<FutureEntity> findByExpirationDateBefore(LocalDateTime expirationDate);

    /**
     * Находит фьючерсы, истекающие после указанной даты
     * 
     * <p>
     * Возвращает все фьючерсы, дата экспирации которых больше указанной даты.
     * </p>
     * 
     * @param expirationDate минимальная дата экспирации
     * @return список фьючерсов, истекающих после указанной даты
     */
    List<FutureEntity> findByExpirationDateAfter(LocalDateTime expirationDate);

    /**
     * Находит активные фьючерсы (не истекшие)
     * 
     * <p>
     * Возвращает все фьючерсы, дата экспирации которых больше текущей даты.
     * </p>
     * 
     * @param currentDate текущая дата
     * @return список активных (не истекших) фьючерсов
     */
    @Query("SELECT f FROM FutureEntity f WHERE f.expirationDate > :currentDate")
    List<FutureEntity> findActiveFutures(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Находит истекшие фьючерсы
     * 
     * <p>
     * Возвращает все фьючерсы, дата экспирации которых меньше или равна текущей
     * дате.
     * </p>
     * 
     * @param currentDate текущая дата
     * @return список истекших фьючерсов
     */
    @Query("SELECT f FROM FutureEntity f WHERE f.expirationDate <= :currentDate")
    List<FutureEntity> findExpiredFutures(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Подсчитывает количество фьючерсов на указанной бирже
     * 
     * <p>
     * Возвращает общее количество фьючерсов, торгуемых на указанной бирже.
     * </p>
     * 
     * @param exchange название биржи
     * @return количество фьючерсов на бирже
     */
    long countByExchange(String exchange);

    /**
     * Проверяет существование фьючерса по FIGI
     * 
     * <p>
     * Возвращает true, если фьючерс с указанным FIGI существует в базе данных.
     * </p>
     * 
     * @param figi уникальный идентификатор инструмента
     * @return true если фьючерс существует, false в противном случае
     */
    boolean existsByFigi(String figi);

}
