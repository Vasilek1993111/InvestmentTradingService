package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.ClosePriceEveningSessionDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.repository.ClosePriceEveningSessionRepository;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис для работы с ценами закрытия вечерней сессии
 *
 * <p>
 * Предоставляет методы для получения цен закрытия вечерней сессии из кэша с
 * автоматическим
 * fallback на базу данных. Реализует принцип единственной ответственности
 * (SRP) - отвечает только за работу с ценами закрытия вечерней сессии.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех цен закрытия вечерней сессии из кэша с fallback на БД</li>
 * <li>Получение цен закрытия вечерней сессии только из кэша</li>
 * <li>Поиск цен закрытия вечерней сессии по FIGI</li>
 * <li>Получение статистики по ценам закрытия вечерней сессии</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class ClosePriceEveningSessionService extends AbstractCacheableInstrumentService<ClosePriceEveningSessionDTO> {

    /** Репозиторий для работы с ценами закрытия вечерней сессии */
    private final ClosePriceEveningSessionRepository closePriceEveningSessionRepository;

    /**
     * Конструктор сервиса цен закрытия вечерней сессии
     *
     * @param cacheManager                       менеджер кэша Spring
     * @param instrumentCacheManager             менеджер кэша инструментов
     * @param closePriceEveningSessionRepository репозиторий для работы с ценами
     *                                           закрытия вечерней сессии
     */
    public ClosePriceEveningSessionService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            ClosePriceEveningSessionRepository closePriceEveningSessionRepository) {
        super(cacheManager, instrumentCacheManager, InstrumentType.CLOSE_PRICES_EVENING_SESSION);
        this.closePriceEveningSessionRepository = closePriceEveningSessionRepository;
    }

    /**
     * Извлекает FIGI из объекта цены закрытия вечерней сессии
     *
     * @param closePriceEveningSession объект цены закрытия вечерней сессии
     * @return FIGI инструмента
     */
    @Override
    protected String getFigiFromItem(ClosePriceEveningSessionDTO closePriceEveningSession) {
        return closePriceEveningSession.figi();
    }
}
