package com.example.investmenttradingservice.service.cacheInstrumentsService.impl;


import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.DTO.ShareDTO;
import com.example.investmenttradingservice.enums.InstrumentType;
import com.example.investmenttradingservice.repository.ShareRepository;
import com.example.investmenttradingservice.service.cacheInstrumentsService.abstracts.AbstractCacheableInstrumentService;
import com.example.investmenttradingservice.service.cacheInstrumentsService.interfaces.InstrumentCacheManager;

/**
 * Сервис для работы с акциями
 *
 * <p>
 * Предоставляет методы для получения акций из кэша с автоматическим
 * fallback на базу данных. Реализует принцип единственной ответственности
 * (SRP) - отвечает только за работу с акциями.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Получение всех акций из кэша с fallback на БД</li>
 * <li>Получение акций только из кэша</li>
 * <li>Поиск акций по FIGI</li>
 * <li>Получение статистики по акциям</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class ShareService extends AbstractCacheableInstrumentService<ShareDTO> {

    /** Репозиторий для работы с акциями */
    private final ShareRepository shareRepository;

    /**
     * Конструктор сервиса акций
     *
     * @param cacheManager           менеджер кэша Spring
     * @param instrumentCacheManager менеджер кэша инструментов
     * @param shareRepository        репозиторий для работы с акциями
     */
    public ShareService(CacheManager cacheManager,
            InstrumentCacheManager instrumentCacheManager,
            ShareRepository shareRepository) {
        super(cacheManager, instrumentCacheManager, InstrumentType.SHARES);
        this.shareRepository = shareRepository;
    }

    /**
     * Извлекает FIGI из объекта акции
     *
     * @param share объект акции
     * @return FIGI акции
     */
    @Override
    protected String getFigiFromItem(ShareDTO share) {
        return share.figi();
    }
}
