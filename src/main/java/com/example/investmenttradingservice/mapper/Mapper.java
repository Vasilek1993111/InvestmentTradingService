package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.ShareDTO;
import com.example.investmenttradingservice.DTO.FutureDTO;
import com.example.investmenttradingservice.DTO.IndicativeDTO;
import com.example.investmenttradingservice.Entity.ShareEntity;
import com.example.investmenttradingservice.Entity.FutureEntity;
import com.example.investmenttradingservice.Entity.IndicativeEntity;
import com.example.investmenttradingservice.util.TimeZoneUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования Entity в DTO и обратно
 */
@Component
public class Mapper {

    /**
     * Преобразует ShareEntity в ShareDTO
     * 
     * @param entity ShareEntity для преобразования
     * @return ShareDTO
     */
    public ShareDTO toShareDTO(ShareEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ShareDTO(
                entity.getFigi(),
                entity.getTicker(),
                entity.getName(),
                entity.getCurrency(),
                entity.getExchange(),
                entity.getSector(),
                entity.getTradingStatus(),
                entity.getShortEnabled(),
                entity.getAssetUid());
    }

    /**
     * Преобразует ShareDTO в ShareEntity
     * 
     * @param dto ShareDTO для преобразования
     * @return ShareEntity
     */
    public ShareEntity toShareEntity(ShareDTO dto) {
        if (dto == null) {
            return null;
        }

        ShareEntity entity = new ShareEntity();
        entity.setFigi(dto.figi());
        entity.setTicker(dto.ticker());
        entity.setName(dto.name());
        entity.setCurrency(dto.currency());
        entity.setExchange(dto.exchange());
        entity.setSector(dto.sector());
        entity.setTradingStatus(dto.tradingStatus());
        entity.setShortEnabled(dto.shortEnabled());
        entity.setAssetUid(dto.assetUid());

        // Устанавливаем временные метки
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return entity;
    }

    /**
     * Обновляет существующую ShareEntity данными из ShareDTO
     * 
     * @param entity существующая ShareEntity для обновления
     * @param dto    ShareDTO с новыми данными
     * @return обновленная ShareEntity
     */
    public ShareEntity updateShareEntity(ShareEntity entity, ShareDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        // Обновляем только изменяемые поля, figi остается неизменным
        entity.setTicker(dto.ticker());
        entity.setName(dto.name());
        entity.setCurrency(dto.currency());
        entity.setExchange(dto.exchange());
        entity.setSector(dto.sector());
        entity.setTradingStatus(dto.tradingStatus());
        entity.setShortEnabled(dto.shortEnabled());
        entity.setAssetUid(dto.assetUid());

        // Обновляем время изменения
        entity.setUpdatedAt(LocalDateTime.now());

        return entity;
    }

    /**
     * Преобразует FutureEntity в FutureDTO
     * 
     * @param entity FutureEntity для преобразования
     * @return FutureDTO
     */
    public FutureDTO toFutureDTO(FutureEntity entity) {
        if (entity == null) {
            return null;
        }

        return new FutureDTO(
                entity.getFigi(),
                entity.getTicker(),
                entity.getAssetType(),
                entity.getBasicAsset(),
                entity.getCurrency(),
                entity.getExchange(),
                entity.getShortEnabled(),
                entity.getExpirationDate());
    }

    /**
     * Преобразует FutureDTO в FutureEntity
     * 
     * @param dto FutureDTO для преобразования
     * @return FutureEntity
     */
    public FutureEntity toFutureEntity(FutureDTO dto) {
        if (dto == null) {
            return null;
        }

        FutureEntity entity = new FutureEntity();
        entity.setFigi(dto.figi());
        entity.setTicker(dto.ticker());
        entity.setAssetType(dto.assetType());
        entity.setBasicAsset(dto.basicAsset());
        entity.setCurrency(dto.currency());
        entity.setExchange(dto.exchange());
        entity.setShortEnabled(dto.shortEnabled());
        entity.setExpirationDate(dto.expirationDate());

        // Устанавливаем временные метки
        LocalDateTime now = LocalDateTime.now(TimeZoneUtils.getMoscowZone());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return entity;
    }

    /**
     * Обновляет существующую FutureEntity данными из FutureDTO
     * 
     * @param entity существующая FutureEntity для обновления
     * @param dto    FutureDTO с новыми данными
     * @return обновленная FutureEntity
     */
    public FutureEntity updateFutureEntity(FutureEntity entity, FutureDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        // Обновляем только изменяемые поля, figi остается неизменным
        entity.setTicker(dto.ticker());
        entity.setAssetType(dto.assetType());
        entity.setBasicAsset(dto.basicAsset());
        entity.setCurrency(dto.currency());
        entity.setExchange(dto.exchange());
        entity.setShortEnabled(dto.shortEnabled());
        entity.setExpirationDate(dto.expirationDate());

        // Обновляем время изменения
        entity.setUpdatedAt(LocalDateTime.now(TimeZoneUtils.getMoscowZone()));

        return entity;
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
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toShareDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список ShareDTO в список ShareEntity
     * 
     * @param dtos список ShareDTO для преобразования
     * @return список ShareEntity
     */
    public List<ShareEntity> toShareEntityList(List<ShareDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toShareEntity)
                .collect(Collectors.toList());
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
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toFutureDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список FutureDTO в список FutureEntity
     * 
     * @param dtos список FutureDTO для преобразования
     * @return список FutureEntity
     */
    public List<FutureEntity> toFutureEntityList(List<FutureDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toFutureEntity)
                .collect(Collectors.toList());
    }

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
        if (list == null) {
            return null;
        }

        return list.stream()
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    /**
     * Проверяет, является ли список пустым или null
     * 
     * @param list список для проверки
     * @return true если список null или пустой
     */
    public <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    // ========== Методы для индикативов ==========

    /**
     * Преобразует IndicativeEntity в IndicativeDTO
     *
     * @param entity IndicativeEntity для преобразования
     * @return IndicativeDTO
     */
    public IndicativeDTO toIndicativeDTO(IndicativeEntity entity) {
        if (entity == null) {
            return null;
        }
        return new IndicativeDTO(
                entity.getFigi(),
                entity.getTicker(),
                entity.getName(),
                entity.getCurrency(),
                entity.getExchange(),
                entity.getClassCode(),
                entity.getUid(),
                entity.getSellAvailableFlag(),
                entity.getBuyAvailableFlag());
    }

    /**
     * Преобразует список IndicativeEntity в список IndicativeDTO
     *
     * @param entities список IndicativeEntity для преобразования
     * @return список IndicativeDTO
     */
    public List<IndicativeDTO> toIndicativeDTOList(List<IndicativeEntity> entities) {
        if (isEmpty(entities)) {
            return List.of();
        }
        return entities.stream()
                .map(this::toIndicativeDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует IndicativeDTO в IndicativeEntity
     *
     * @param dto IndicativeDTO для преобразования
     * @return IndicativeEntity
     */
    public IndicativeEntity toIndicativeEntity(IndicativeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new IndicativeEntity(
                dto.figi(),
                dto.ticker(),
                dto.name(),
                dto.currency(),
                dto.exchange(),
                dto.classCode(),
                dto.uid(),
                dto.sellAvailableFlag(),
                dto.buyAvailableFlag());
    }

    /**
     * Преобразует список IndicativeDTO в список IndicativeEntity
     *
     * @param dtos список IndicativeDTO для преобразования
     * @return список IndicativeEntity
     */
    public List<IndicativeEntity> toIndicativeEntityList(List<IndicativeDTO> dtos) {
        if (isEmpty(dtos)) {
            return List.of();
        }
        return dtos.stream()
                .map(this::toIndicativeEntity)
                .collect(Collectors.toList());
    }
}
