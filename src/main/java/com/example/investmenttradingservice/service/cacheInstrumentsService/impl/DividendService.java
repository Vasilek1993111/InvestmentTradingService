package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.DividendDto;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.repository.DivedendsRepository;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис для работы с дивидендами
 *
 * <p>
 * Предоставляет методы для получения дивидендов из кэша с автоматическим
 * fallback на базу данных. Реализует принцип единственной ответственности
 * (SRP) - отвечает только за работу с дивидендами.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех дивидендов из кэша с fallback на БД</li>
 * <li>Получение дивидендов только из кэша</li>
 * <li>Поиск дивидендов по FIGI</li>
 * <li>Получение статистики по дивидендам</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class DividendService extends AbstractCacheableInstrumentService<DividendDto> {

    /** Репозиторий для работы с дивидендами */
    private final DivedendsRepository divedendsRepository;

    /**
     * Конструктор сервиса дивидендов
     *
     * @param cacheManager           менеджер кэша Spring
     * @param instrumentCacheManager менеджер кэша инструментов
     * @param divedendsRepository    репозиторий для работы с дивидендами
     */
    public DividendService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            DivedendsRepository divedendsRepository) {
        super(cacheManager, instrumentCacheManager, InstrumentType.DIVIDENDS);
        this.divedendsRepository = divedendsRepository;
    }

    /**
     * Извлекает FIGI из объекта дивиденда
     *
     * @param dividend объект дивиденда
     * @return FIGI инструмента
     */
    @Override
    protected String getFigiFromItem(DividendDto dividend) {
        return dividend.figi();
    }
}
