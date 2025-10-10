package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.ClosePriceDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.repository.ClosePriceRepository;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис для работы с ценами закрытия
 *
 * <p>
 * Предоставляет методы для получения цен закрытия из кэша с автоматическим
 * fallback на базу данных. Реализует принцип единственной ответственности
 * (SRP) - отвечает только за работу с ценами закрытия.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех цен закрытия из кэша с fallback на БД</li>
 * <li>Получение цен закрытия только из кэша</li>
 * <li>Поиск цен закрытия по FIGI</li>
 * <li>Получение статистики по ценам закрытия</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class ClosePriceService extends AbstractCacheableInstrumentService<ClosePriceDTO> {

    /** Репозиторий для работы с ценами закрытия */
    private final ClosePriceRepository closePriceRepository;

    /**
     * Конструктор сервиса цен закрытия
     *
     * @param cacheManager           менеджер кэша Spring
     * @param instrumentCacheManager менеджер кэша инструментов
     * @param closePriceRepository   репозиторий для работы с ценами закрытия
     */
    public ClosePriceService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            ClosePriceRepository closePriceRepository) {
        super(cacheManager, instrumentCacheManager, InstrumentType.CLOSE_PRICES);
        this.closePriceRepository = closePriceRepository;
    }

    /**
     * Извлекает FIGI из объекта цены закрытия
     *
     * @param closePrice объект цены закрытия
     * @return FIGI инструмента
     */
    @Override
    protected String getFigiFromItem(ClosePriceDTO closePrice) {
        return closePrice.figi();
    }
}
