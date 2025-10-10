package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.OpenPriceDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.repository.OpenPriceRepositrory;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис для работы с ценами открытия
 *
 * <p>
 * Предоставляет методы для получения цен открытия из кэша с автоматическим
 * fallback на базу данных. Реализует принцип единственной ответственности
 * (SRP) - отвечает только за работу с ценами открытия.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех цен открытия из кэша с fallback на БД</li>
 * <li>Получение цен открытия только из кэша</li>
 * <li>Поиск цен открытия по FIGI</li>
 * <li>Получение статистики по ценам открытия</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class OpenPriceService extends AbstractCacheableInstrumentService<OpenPriceDTO> {

    /** Репозиторий для работы с ценами открытия */
    private final OpenPriceRepositrory openPriceRepository;

    /**
     * Конструктор сервиса цен открытия
     *
     * @param cacheManager           менеджер кэша Spring
     * @param instrumentCacheManager менеджер кэша инструментов
     * @param openPriceRepository    репозиторий для работы с ценами открытия
     */
    public OpenPriceService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            OpenPriceRepositrory openPriceRepository) {
        super(cacheManager, instrumentCacheManager, InstrumentType.OPEN_PRICES);
        this.openPriceRepository = openPriceRepository;
    }

    /**
     * Извлекает FIGI из объекта цены открытия
     *
     * @param openPrice объект цены открытия
     * @return FIGI инструмента
     */
    @Override
    protected String getFigiFromItem(OpenPriceDTO openPrice) {
        return openPrice.figi();
    }
}
