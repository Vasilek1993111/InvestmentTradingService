package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.ClosePriceDTO;
import com.example.investmenttradingservice.entity.ClosePriceEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования ClosePriceEntity в ClosePriceDTO и обратно
 * 
 * <p>
 * Обеспечивает конвертацию между сущностями цен закрытия и их DTO
 * представлениями.
 * Включает методы для работы с единичными объектами и списками.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Component
public class ClosePriceMapper {

    /**
     * Преобразует ClosePriceEntity в ClosePriceDTO
     *
     * @param entity ClosePriceEntity для преобразования
     * @return ClosePriceDTO или null если entity равен null
     */
    public ClosePriceDTO toDTO(ClosePriceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ClosePriceDTO(
                entity.getId().getPriceDate(),
                entity.getId().getFigi(),
                entity.getInstrumentType(),
                entity.getClosePrice(),
                entity.getCurrency(),
                entity.getExchange(),
                entity.getCreatedAt().toLocalDateTime(),
                entity.getUpdatedAt().toLocalDateTime());
    }

    /**
     * Преобразует ClosePriceDTO в ClosePriceEntity
     *
     * @param dto ClosePriceDTO для преобразования
     * @return ClosePriceEntity или null если dto равен null
     */
    public ClosePriceEntity toEntity(ClosePriceDTO dto) {
        if (dto == null) {
            return null;
        }

        return new ClosePriceEntity(
                dto.priceDate(),
                dto.figi(),
                dto.instrumentType(),
                dto.closePrice(),
                dto.currency(),
                dto.exchange());
    }

    /**
     * Преобразует список ClosePriceEntity в список ClosePriceDTO
     *
     * @param entities список ClosePriceEntity для преобразования
     * @return список ClosePriceDTO или пустой список если entities пустой или null
     */
    public List<ClosePriceDTO> toDTOList(List<ClosePriceEntity> entities) {
        if (isEmpty(entities)) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список ClosePriceDTO в список ClosePriceEntity
     *
     * @param dtos список ClosePriceDTO для преобразования
     * @return список ClosePriceEntity или пустой список если dtos пустой или null
     */
    public List<ClosePriceEntity> toEntityList(List<ClosePriceDTO> dtos) {
        if (isEmpty(dtos)) {
            return List.of();
        }
        return dtos.stream()
                .map(this::toEntity)
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

