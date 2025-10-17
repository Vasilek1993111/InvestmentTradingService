package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.FutureDTO;
import com.example.investmenttradingservice.entity.FutureEntity;
import com.example.investmenttradingservice.util.TimeZoneUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования FutureEntity в FutureDTO и обратно
 * 
 * <p>
 * Обеспечивает конвертацию между сущностями фьючерсов и их DTO представлениями.
 * Включает методы для работы с единичными объектами и списками.
 * Использует московскую временную зону для корректной работы с временными
 * метками.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Component
public class FutureMapper {

    /**
     * Преобразует FutureEntity в FutureDTO
     * 
     * @param entity FutureEntity для преобразования
     * @return FutureDTO или null если entity равен null
     */
    public FutureDTO toDTO(FutureEntity entity) {
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
                entity.getExpirationDate(),
                entity.getLot(),
                entity.getMinPriceIncrement());
    }

    /**
     * Преобразует FutureDTO в FutureEntity
     * 
     * @param dto FutureDTO для преобразования
     * @return FutureEntity или null если dto равен null
     */
    public FutureEntity toEntity(FutureDTO dto) {
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
        entity.setLot(dto.lot());
        entity.setMinPriceIncrement(dto.minPriceIncrement());
        // Устанавливаем временные метки в московской временной зоне
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
     * @return обновленная FutureEntity или исходная entity если dto равен null
     */
    public FutureEntity updateEntity(FutureEntity entity, FutureDTO dto) {
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
        entity.setMinPriceIncrement(dto.minPriceIncrement());
        // Обновляем время изменения в московской временной зоне
        entity.setUpdatedAt(LocalDateTime.now(TimeZoneUtils.getMoscowZone()));

        return entity;
    }

    /**
     * Преобразует список FutureEntity в список FutureDTO
     * 
     * @param entities список FutureEntity для преобразования
     * @return список FutureDTO или null если entities равен null
     */
    public List<FutureDTO> toDTOList(List<FutureEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список FutureDTO в список FutureEntity
     * 
     * @param dtos список FutureDTO для преобразования
     * @return список FutureEntity или null если dtos равен null
     */
    public List<FutureEntity> toEntityList(List<FutureDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Фильтрует null значения из списка
     * 
     * @param list список для фильтрации
     * @return отфильтрованный список без null значений или null если исходный
     *         список null
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
}
