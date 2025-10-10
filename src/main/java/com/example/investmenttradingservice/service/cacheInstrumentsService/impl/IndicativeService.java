package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.IndicativeDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.repository.Indicativerepository;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис для работы с индикативами
 *
 * <p>
 * Предоставляет методы для получения индикативов из кэша с автоматическим
 * fallback на базу данных. Реализует принцип единственной ответственности
 * (SRP) - отвечает только за работу с индикативами.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех индикативов из кэша с fallback на БД</li>
 * <li>Получение индикативов только из кэша</li>
 * <li>Поиск индикативов по FIGI</li>
 * <li>Получение статистики по индикативам</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class IndicativeService extends AbstractCacheableInstrumentService<IndicativeDTO> {

    /** Репозиторий для работы с индикативами */
    private final Indicativerepository indicativeRepository;

    /**
     * Конструктор сервиса индикативов
     *
     * @param cacheManager           менеджер кэша Spring
     * @param instrumentCacheManager менеджер кэша инструментов
     * @param indicativeRepository   репозиторий для работы с индикативами
     */
    public IndicativeService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            Indicativerepository indicativeRepository) {
        super(cacheManager, instrumentCacheManager, InstrumentType.INDICATIVES);
        this.indicativeRepository = indicativeRepository;
    }

    /**
     * Извлекает FIGI из объекта индикатива
     *
     * @param indicative объект индикатива
     * @return FIGI индикатива
     */
    @Override
    protected String getFigiFromItem(IndicativeDTO indicative) {
        return indicative.figi();
    }
}
