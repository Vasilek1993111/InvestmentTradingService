package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.ClosePriceEveningSessionDTO;
import com.example.investmenttradingservice.entity.ClosePriceEveningSessionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования ClosePriceEveningSessionEntity в
 * ClosePriceEveningSessionDTO и обратно
 * 
 * <p>
 * Обеспечивает конвертацию между сущностями цен закрытия вечерней сессии и их
 * DTO представлениями.
 * Включает методы для работы с единичными объектами и списками.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Component
public class ClosePriceEveningSessionMapper {

    /**
     * Преобразует ClosePriceEveningSessionEntity в ClosePriceEveningSessionDTO
     *
     * @param entity ClosePriceEveningSessionEntity для преобразования
     * @return ClosePriceEveningSessionDTO или null если entity равен null
     */
    public ClosePriceEveningSessionDTO toDTO(ClosePriceEveningSessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ClosePriceEveningSessionDTO(
                entity.getPriceDate(),
                entity.getFigi(),
                entity.getClosePrice(),
                entity.getInstrumentType(),
                entity.getCurrency(),
                entity.getExchange());
    }

    /**
     * Преобразует ClosePriceEveningSessionDTO в ClosePriceEveningSessionEntity
     *
     * @param dto ClosePriceEveningSessionDTO для преобразования
     * @return ClosePriceEveningSessionEntity или null если dto равен null
     */
    public ClosePriceEveningSessionEntity toEntity(ClosePriceEveningSessionDTO dto) {
        if (dto == null) {
            return null;
        }
        return new ClosePriceEveningSessionEntity(
                dto.priceDate(),
                dto.figi(),
                dto.closePrice(),
                dto.instrumentType(),
                dto.currency(),
                dto.exchange());
    }

    /**
     * Преобразует список ClosePriceEveningSessionEntity в список
     * ClosePriceEveningSessionDTO
     *
     * @param entities список ClosePriceEveningSessionEntity для преобразования
     * @return список ClosePriceEveningSessionDTO или пустой список если entities
     *         пустой или null
     */
    public List<ClosePriceEveningSessionDTO> toDTOList(List<ClosePriceEveningSessionEntity> entities) {
        if (isEmpty(entities)) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список ClosePriceEveningSessionDTO в список
     * ClosePriceEveningSessionEntity
     *
     * @param dtos список ClosePriceEveningSessionDTO для преобразования
     * @return список ClosePriceEveningSessionEntity или пустой список если dtos
     *         пустой или null
     */
    public List<ClosePriceEveningSessionEntity> toEntityList(List<ClosePriceEveningSessionDTO> dtos) {
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

