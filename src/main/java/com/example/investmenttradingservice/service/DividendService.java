package com.example.investmenttradingservice.service;

import com.example.investmenttradingservice.DTO.DividendDto;
import com.example.investmenttradingservice.entity.DividendEntity;
import com.example.investmenttradingservice.mapper.Mapper;
import com.example.investmenttradingservice.repository.DivedendsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Сервис для работы с дивидендами
 * 
 * <p>
 * Реализует бизнес-логику загрузки дивидендов согласно требованиям:
 * - Обычные дни: declared_date между вчера и сегодня
 * - Воскресенье: declared_date между позавчера и сегодня
 * - Дивидендная отсечка: record_date = declared_date - 1
 * - Цель: дивиденды на сегодня или завтра
 * </p>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class DividendService {

    @Autowired
    private DivedendsRepository dividendRepository;

    @Autowired
    private Mapper mapper;

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
     * <li>payment_date BETWEEN сегодня AND завтра</li>
     * </ul>
     * 
     * @return список дивидендов на сегодня или завтра
     */
    public List<DividendDto> getDividendsForTodayAndTomorrow() {
        List<DividendEntity> entities = dividendRepository.findDividendsForTodayAndTomorrow();
        return mapper.toDividendDtoList(entities);
    }

    /**
     * Получает дивиденды по конкретному инструменту
     * 
     * @param figi уникальный идентификатор финансового инструмента
     * @return список дивидендов для указанного инструмента
     */
    public List<DividendDto> getDividendsByFigi(String figi) {
        List<DividendEntity> entities = dividendRepository.findDividendsByFigi(figi);
        return mapper.toDividendDtoList(entities);
    }

    /**
     * Получает дивиденды по дате выплаты
     * 
     * @param paymentDate дата выплаты дивидендов
     * @return список дивидендов на указанную дату
     */
    public List<DividendDto> getDividendsByPaymentDate(LocalDate paymentDate) {
        List<DividendEntity> entities = dividendRepository.findDividendsByPaymentDate(paymentDate);
        return mapper.toDividendDtoList(entities);
    }

    /**
     * Получает дивиденды в указанной валюте
     * 
     * @param currency валюта дивидендов
     * @return список дивидендов в указанной валюте
     */
    public List<DividendDto> getDividendsByCurrency(String currency) {
        List<DividendEntity> entities = dividendRepository.findDividendsByCurrency(currency);
        return mapper.toDividendDtoList(entities);
    }

    /**
     * Получает дивиденды указанного типа
     * 
     * @param dividendType тип дивидендов
     * @return список дивидендов указанного типа
     */
    public List<DividendDto> getDividendsByType(String dividendType) {
        List<DividendEntity> entities = dividendRepository.findDividendsByType(dividendType);
        return mapper.toDividendDtoList(entities);
    }

    /**
     * Проверяет наличие актуальных дивидендов для инструмента
     * 
     * @param figi уникальный идентификатор финансового инструмента
     * @return true если есть актуальные дивиденды
     */
    public boolean hasActiveDividends(String figi) {
        return dividendRepository.existsActiveDividendsByFigi(figi);
    }

    /**
     * Получает статистику по дивидендам
     * 
     * @return объект с статистикой: [количество, общая_сумма,
     *         количество_инструментов]
     */
    public Object[] getDividendsStatistics() {
        return dividendRepository.getDividendsStatistics();
    }

    /**
     * Получает количество инструментов с актуальными дивидендами
     * 
     * @return количество инструментов с дивидендами
     */
    public long getInstrumentsWithDividendsCount() {
        return dividendRepository.countInstrumentsWithActiveDividends();
    }
}