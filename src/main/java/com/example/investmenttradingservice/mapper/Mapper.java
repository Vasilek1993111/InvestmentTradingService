package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.*;
import com.example.investmenttradingservice.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Главный Mapper для преобразования Entity в DTO и обратно
 * 
 * <p>
 * Делегирует работу специализированным мапперам для каждого типа сущностей.
 * Обеспечивает единую точку входа для всех преобразований в приложении.
 * </p>
 */
@Component
public class Mapper {

    @Autowired
    private ShareMapper shareMapper;

    @Autowired
    private FutureMapper futureMapper;

    @Autowired
    private IndicativeMapper indicativeMapper;

    @Autowired
    private ClosePriceMapper closePriceMapper;

    @Autowired
    private OpenPriceMapper openPriceMapper;

    @Autowired
    private LastPriceMapper lastPriceMapper;

    @Autowired
    private ClosePriceEveningSessionMapper closePriceEveningSessionMapper;

    @Autowired
    private DividendMapper dividendMapper;

 

    // ===========================================
    // Методы для работы с Share (делегирование)
    // ===========================================

    /**
     * Преобразует ShareEntity в ShareDTO
     * 
     * @param entity ShareEntity для преобразования
     * @return ShareDTO
     */
    public ShareDTO toShareDTO(ShareEntity entity) {
        return shareMapper.toDTO(entity);
    }

    /**
     * Преобразует ShareDTO в ShareEntity
     * 
     * @param dto ShareDTO для преобразования
     * @return ShareEntity
     */
    public ShareEntity toShareEntity(ShareDTO dto) {
        return shareMapper.toEntity(dto);
    }

    /**
     * Обновляет существующую ShareEntity данными из ShareDTO
     * 
     * @param entity существующая ShareEntity для обновления
     * @param dto    ShareDTO с новыми данными
     * @return обновленная ShareEntity
     */
    public ShareEntity updateShareEntity(ShareEntity entity, ShareDTO dto) {
        return shareMapper.updateEntity(entity, dto);
    }

    // ===========================================
    // Методы для работы с Future (делегирование)
    // ===========================================

    /**
     * Преобразует FutureEntity в FutureDTO
     * 
     * @param entity FutureEntity для преобразования
     * @return FutureDTO
     */
    public FutureDTO toFutureDTO(FutureEntity entity) {
        return futureMapper.toDTO(entity);
    }

    /**
     * Преобразует FutureDTO в FutureEntity
     * 
     * @param dto FutureDTO для преобразования
     * @return FutureEntity
     */
    public FutureEntity toFutureEntity(FutureDTO dto) {
        return futureMapper.toEntity(dto);
    }

    /**
     * Обновляет существующую FutureEntity данными из FutureDTO
     * 
     * @param entity существующая FutureEntity для обновления
     * @param dto    FutureDTO с новыми данными
     * @return обновленная FutureEntity
     */
    public FutureEntity updateFutureEntity(FutureEntity entity, FutureDTO dto) {
        return futureMapper.updateEntity(entity, dto);
    }

    // ===========================================
    // Методы для работы со списками Shares
    // ===========================================

    /**
     * Преобразует список ShareEntity в список ShareDTO
     * 
     * @param entities список ShareEntity для преобразования
     * @return список ShareDTO
     */
    public List<ShareDTO> toShareDTOList(List<ShareEntity> entities) {
        return shareMapper.toDTOList(entities);
    }

    /**
     * Преобразует список ShareDTO в список ShareEntity
     * 
     * @param dtos список ShareDTO для преобразования
     * @return список ShareEntity
     */
    public List<ShareEntity> toShareEntityList(List<ShareDTO> dtos) {
        return shareMapper.toEntityList(dtos);
    }

    // ===========================================
    // Методы для работы со списками Futures
    // ===========================================

    /**
     * Преобразует список FutureEntity в список FutureDTO
     * 
     * @param entities список FutureEntity для преобразования
     * @return список FutureDTO
     */
    public List<FutureDTO> toFutureDTOList(List<FutureEntity> entities) {
        return futureMapper.toDTOList(entities);
    }

    /**
     * Преобразует список FutureDTO в список FutureEntity
     * 
     * @param dtos список FutureDTO для преобразования
     * @return список FutureEntity
     */
    public List<FutureEntity> toFutureEntityList(List<FutureDTO> dtos) {
        return futureMapper.toEntityList(dtos);
    }

    // ===========================================
    // Методы для работы с Indicative (делегирование)
    // ===========================================

    /**
     * Преобразует IndicativeEntity в IndicativeDTO
     *
     * @param entity IndicativeEntity для преобразования
     * @return IndicativeDTO
     */
    public IndicativeDTO toIndicativeDTO(IndicativeEntity entity) {
        return indicativeMapper.toDTO(entity);
    }

    /**
     * Преобразует список IndicativeEntity в список IndicativeDTO
     *
     * @param entities список IndicativeEntity для преобразования
     * @return список IndicativeDTO
     */
    public List<IndicativeDTO> toIndicativeDTOList(List<IndicativeEntity> entities) {
        return indicativeMapper.toDTOList(entities);
    }

    /**
     * Преобразует IndicativeDTO в IndicativeEntity
     *
     * @param dto IndicativeDTO для преобразования
     * @return IndicativeEntity
     */
    public IndicativeEntity toIndicativeEntity(IndicativeDTO dto) {
        return indicativeMapper.toEntity(dto);
    }

    /**
     * Преобразует список IndicativeDTO в список IndicativeEntity
     *
     * @param dtos список IndicativeDTO для преобразования
     * @return список IndicativeEntity
     */
    public List<IndicativeEntity> toIndicativeEntityList(List<IndicativeDTO> dtos) {
        return indicativeMapper.toEntityList(dtos);
    }

    // ===========================================
    // Методы для работы с ClosePrice (делегирование)
    // ===========================================

    /**
     * Преобразует ClosePriceEntity в ClosePriceDTO
     *
     * @param entity ClosePriceEntity для преобразования
     * @return ClosePriceDTO
     */
    public ClosePriceDTO toClosePriceDTO(ClosePriceEntity entity) {
        return closePriceMapper.toDTO(entity);
    }

    /**
     * Преобразует ClosePriceDTO в ClosePriceEntity
     *
     * @param dto ClosePriceDTO для преобразования
     * @return ClosePriceEntity
     */
    public ClosePriceEntity toClosePriceEntity(ClosePriceDTO dto) {
        return closePriceMapper.toEntity(dto);
    }

    /**
     * Преобразует список ClosePriceEntity в список ClosePriceDTO
     *
     * @param entities список ClosePriceEntity для преобразования
     * @return список ClosePriceDTO
     */
    public List<ClosePriceDTO> toClosePriceDTOList(List<ClosePriceEntity> entities) {
        return closePriceMapper.toDTOList(entities);
    }

    /**
     * Преобразует список ClosePriceDTO в список ClosePriceEntity
     *
     * @param dtos список ClosePriceDTO для преобразования
     * @return список ClosePriceEntity
     */
    public List<ClosePriceEntity> toClosePriceEntityList(List<ClosePriceDTO> dtos) {
        return closePriceMapper.toEntityList(dtos);
    }

    // ===========================================
    // Методы для работы с OpenPrice (делегирование)
    // ===========================================

    /**
     * Преобразует OpenPriceEntity в OpenPriceDTO
     *
     * @param entity OpenPriceEntity для преобразования
     * @return OpenPriceDTO
     */
    public OpenPriceDTO toOpenPriceDTO(OpenPriceEntity entity) {
        return openPriceMapper.toDTO(entity);
    }

    /**
     * Преобразует OpenPriceDTO в OpenPriceEntity
     *
     * @param dto OpenPriceDTO для преобразования
     * @return OpenPriceEntity
     */
    public OpenPriceEntity toOpenPriceEntity(OpenPriceDTO dto) {
        return openPriceMapper.toEntity(dto);
    }

    /**
     * Преобразует список OpenPriceEntity в список OpenPriceDTO
     *
     * @param entities список OpenPriceEntity для преобразования
     * @return список OpenPriceDTO
     */
    public List<OpenPriceDTO> toOpenPriceDTOList(List<OpenPriceEntity> entities) {
        return openPriceMapper.toDTOList(entities);
    }

    /**
     * Преобразует список OpenPriceDTO в список OpenPriceEntity
     *
     * @param dtos список OpenPriceDTO для преобразования
     * @return список OpenPriceEntity
     */
    public List<OpenPriceEntity> toOpenPriceEntityList(List<OpenPriceDTO> dtos) {
        return openPriceMapper.toEntityList(dtos);
    }

    // ===========================================
    // Методы для работы с ClosePriceEveningSession (делегирование)
    // ===========================================

    /**
     * Преобразует ClosePriceEveningSessionEntity в ClosePriceEveningSessionDTO
     *
     * @param entity ClosePriceEveningSessionEntity для преобразования
     * @return ClosePriceEveningSessionDTO
     */
    public ClosePriceEveningSessionDTO toClosePriceEveningSessionDTO(ClosePriceEveningSessionEntity entity) {
        return closePriceEveningSessionMapper.toDTO(entity);
    }

    /**
     * Преобразует ClosePriceEveningSessionDTO в ClosePriceEveningSessionEntity
     *
     * @param dto ClosePriceEveningSessionDTO для преобразования
     * @return ClosePriceEveningSessionEntity
     */
    public ClosePriceEveningSessionEntity toClosePriceEveningSessionEntity(ClosePriceEveningSessionDTO dto) {
        return closePriceEveningSessionMapper.toEntity(dto);
    }

    /**
     * Преобразует список ClosePriceEveningSessionEntity в список
     * ClosePriceEveningSessionDTO
     *
     * @param entities список ClosePriceEveningSessionEntity для преобразования
     * @return список ClosePriceEveningSessionDTO
     */
    public List<ClosePriceEveningSessionDTO> toClosePriceEveningSessionDTOList(
            List<ClosePriceEveningSessionEntity> entities) {
        return closePriceEveningSessionMapper.toDTOList(entities);
    }

    /**
     * Преобразует список ClosePriceEveningSessionDTO в список
     * ClosePriceEveningSessionEntity
     *
     * @param dtos список ClosePriceEveningSessionDTO для преобразования
     * @return список ClosePriceEveningSessionEntity
     */
    public List<ClosePriceEveningSessionEntity> toClosePriceEveningSessionEntityList(
            List<ClosePriceEveningSessionDTO> dtos) {
        return closePriceEveningSessionMapper.toEntityList(dtos);
    }

    // ===========================================
    // Методы для работы с LastPrice (делегирование)
    // ===========================================

    /**
     * Преобразует LastPriceEntity в LastPriceDTO
     *
     * @param entity LastPriceEntity для преобразования
     * @return LastPriceDTO
     */
    public LastPriceDTO toLastPriceDTO(LastPriceEntity entity) {
        return lastPriceMapper.toDTO(entity);
    }

    /**
     * Преобразует LastPriceDTO в LastPriceEntity
     *
     * @param dto LastPriceDTO для преобразования
     * @return LastPriceEntity
     */
    public LastPriceEntity toLastPriceEntity(LastPriceDTO dto) {
        return lastPriceMapper.toEntity(dto);
    }

    /**
     * Преобразует список LastPriceEntity в список LastPriceDTO
     *
     * @param entities список LastPriceEntity для преобразования
     * @return список LastPriceDTO
     */
    public List<LastPriceDTO> toLastPriceDTOList(List<LastPriceEntity> entities) {
        return lastPriceMapper.toDTOList(entities);
    }

    /**
     * Преобразует список LastPriceDTO в список LastPriceEntity
     *
     * @param dtos список LastPriceDTO для преобразования
     * @return список LastPriceEntity
     */
    public List<LastPriceEntity> toLastPriceEntityList(List<LastPriceDTO> dtos) {
        return lastPriceMapper.toEntityList(dtos);
    }

    // ===========================================
    // Методы для работы с Dividend (делегирование)
    // ===========================================

    /**
     * Преобразует DividendEntity в DividendDto
     *
     * @param entity DividendEntity для преобразования
     * @return DividendDto
     */
    public DividendDto toDividendDto(DividendEntity entity) {
        return dividendMapper.toDTO(entity);
    }

    /**
     * Преобразует DividendDto в DividendEntity
     *
     * @param dto DividendDto для преобразования
     * @return DividendEntity
     */
    public DividendEntity toDividendEntity(DividendDto dto) {
        return dividendMapper.toEntity(dto);
    }

    /**
     * Преобразует список DividendEntity в список DividendDto
     *
     * @param entities список DividendEntity для преобразования
     * @return список DividendDto
     */
    public List<DividendDto> toDividendDtoList(List<DividendEntity> entities) {
        return dividendMapper.toDTOList(entities);
    }

    /**
     * Преобразует список DividendDto в список DividendEntity
     *
     * @param dtos список DividendDto для преобразования
     * @return список DividendEntity
     */
    public List<DividendEntity> toDividendEntityList(List<DividendDto> dtos) {
        return dividendMapper.toEntityList(dtos);
    }

    // ===========================================
    // Методы для работы с Order (делегирование)
    // ===========================================

    
    // ===========================================
    // Универсальные методы для работы со списками
    // ===========================================

    /**
     * Фильтрует null значения из списка
     * 
     * @param list список для фильтрации
     * @return отфильтрованный список без null значений
     */
    public <T> List<T> filterNulls(List<T> list) {
        return shareMapper.filterNulls(list);
    }

    /**
     * Проверяет, является ли список пустым или null
     * 
     * @param list список для проверки
     * @return true если список null или пустой
     */
    public <T> boolean isEmpty(List<T> list) {
        return shareMapper.isEmpty(list);
    }

}
