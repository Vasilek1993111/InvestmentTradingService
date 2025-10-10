package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.FutureDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.repository.FutureRepository;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис для работы с фьючерсами
 *
 * <p>
 * Предоставляет методы для получения фьючерсов из кэша с автоматическим
 * fallback на базу данных. Реализует принцип единственной ответственности
 * (SRP) - отвечает только за работу с фьючерсами.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех фьючерсов из кэша с fallback на БД</li>
 * <li>Получение фьючерсов только из кэша</li>
 * <li>Поиск фьючерсов по FIGI</li>
 * <li>Получение статистики по фьючерсам</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class FutureService extends AbstractCacheableInstrumentService<FutureDTO> {

    /** Репозиторий для работы с фьючерсами */
    private final FutureRepository futureRepository;

    /**
     * Конструктор сервиса фьючерсов
     *
     * @param cacheManager           менеджер кэша Spring
     * @param instrumentCacheManager менеджер кэша инструментов
     * @param futureRepository       репозиторий для работы с фьючерсами
     */
    public FutureService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            FutureRepository futureRepository) {
        super(cacheManager, instrumentCacheManager, InstrumentType.FUTURES);
        this.futureRepository = futureRepository;
    }

    /**
     * Извлекает FIGI из объекта фьючерса
     *
     * @param future объект фьючерса
     * @return FIGI фьючерса
     */
    @Override
    protected String getFigiFromItem(FutureDTO future) {
        return future.figi();
    }
}
