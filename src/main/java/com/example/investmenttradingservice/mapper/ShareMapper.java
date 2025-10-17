package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.ShareDTO;
import com.example.investmenttradingservice.entity.ShareEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования ShareEntity в ShareDTO и обратно
 * 
 * <p>
 * Обеспечивает конвертацию между сущностями акций и их DTO представлениями.
 * Включает методы для работы с единичными объектами и списками.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Component
public class ShareMapper {

    /**
     * Преобразует ShareEntity в ShareDTO
     * 
     * @param entity ShareEntity для преобразования
     * @return ShareDTO или null если entity равен null
     */
    public ShareDTO toDTO(ShareEntity entity) {
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
                entity.getAssetUid(),
                entity.getLot(),
                entity.getMinPriceIncrement());
    }

    /**
     * Преобразует ShareDTO в ShareEntity
     * 
     * @param dto ShareDTO для преобразования
     * @return ShareEntity или null если dto равен null
     */
    public ShareEntity toEntity(ShareDTO dto) {
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
        entity.setLot(dto.lot());
        entity.setMinPriceIncrement(dto.minPriceIncrement());

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
     * @return обновленная ShareEntity или исходная entity если dto равен null
     */
    public ShareEntity updateEntity(ShareEntity entity, ShareDTO dto) {
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
        entity.setMinPriceIncrement(dto.minPriceIncrement());
        // Обновляем время изменения
        entity.setUpdatedAt(LocalDateTime.now());

        return entity;
    }

    /**
     * Преобразует список ShareEntity в список ShareDTO
     * 
     * @param entities список ShareEntity для преобразования
     * @return список ShareDTO или null если entities равен null
     */
    public List<ShareDTO> toDTOList(List<ShareEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список ShareDTO в список ShareEntity
     * 
     * @param dtos список ShareDTO для преобразования
     * @return список ShareEntity или null если dtos равен null
     */
    public List<ShareEntity> toEntityList(List<ShareDTO> dtos) {
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
