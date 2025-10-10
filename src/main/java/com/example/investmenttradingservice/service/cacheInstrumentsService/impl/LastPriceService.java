package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.LastPriceDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.repository.LastPriceRepository;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис для работы с ценами последних сделок
 *
 * <p>
 * Предоставляет методы для получения цен последних сделок из кэша с
 * автоматическим
 * fallback на базу данных. Реализует принцип единственной ответственности
 * (SRP) - отвечает только за работу с ценами последних сделок.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех цен последних сделок из кэша с fallback на БД</li>
 * <li>Получение цен последних сделок только из кэша</li>
 * <li>Поиск цен последних сделок по FIGI</li>
 * <li>Получение статистики по ценам последних сделок</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class LastPriceService extends AbstractCacheableInstrumentService<LastPriceDTO> {

    /** Репозиторий для работы с ценами последних сделок */
    private final LastPriceRepository lastPriceRepository;

    /**
     * Конструктор сервиса цен последних сделок
     *
     * @param cacheManager           менеджер кэша Spring
     * @param instrumentCacheManager менеджер кэша инструментов
     * @param lastPriceRepository    репозиторий для работы с ценами последних
     *                               сделок
     */
    public LastPriceService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            LastPriceRepository lastPriceRepository) {
        super(cacheManager, instrumentCacheManager, InstrumentType.LAST_PRICES);
        this.lastPriceRepository = lastPriceRepository;
    }

    /**
     * Извлекает FIGI из объекта цены последней сделки
     *
     * @param lastPrice объект цены последней сделки
     * @return FIGI инструмента
     */
    @Override
    protected String getFigiFromItem(LastPriceDTO lastPrice) {
        return lastPrice.figi();
    }
}
