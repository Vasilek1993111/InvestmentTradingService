package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.OpenPriceDTO;
import com.example.investmenttradingservice.entity.OpenPriceEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования OpenPriceEntity в OpenPriceDTO и обратно
 * 
 * <p>
 * Обеспечивает конвертацию между сущностями цен открытия и их DTO
 * представлениями.
 * Включает методы для работы с единичными объектами и списками.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Component
public class OpenPriceMapper {

    /**
     * Преобразует OpenPriceEntity в OpenPriceDTO
     *
     * @param entity OpenPriceEntity для преобразования
     * @return OpenPriceDTO или null если entity равен null
     */
    public OpenPriceDTO toDTO(OpenPriceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new OpenPriceDTO(
                entity.getId().getFigi(),
                entity.getId().getPriceDate(),
                entity.getOpenPrice(),
                entity.getInstrumentType(),
                entity.getCurrency(),
                entity.getExchange(),
                entity.getCreatedAt().toLocalDateTime(),
                entity.getUpdatedAt().toLocalDateTime());
    }

    /**
     * Преобразует OpenPriceDTO в OpenPriceEntity
     *
     * @param dto OpenPriceDTO для преобразования
     * @return OpenPriceEntity или null если dto равен null
     */
    public OpenPriceEntity toEntity(OpenPriceDTO dto) {
        if (dto == null) {
            return null;
        }

        return new OpenPriceEntity(
                dto.priceDate(),
                dto.figi(),
                dto.instrumentType(),
                dto.openPrice(),
                dto.currency(),
                dto.exchange());
    }

    /**
     * Преобразует список OpenPriceEntity в список OpenPriceDTO
     *
     * @param entities список OpenPriceEntity для преобразования
     * @return список OpenPriceDTO или пустой список если entities пустой или null
     */
    public List<OpenPriceDTO> toDTOList(List<OpenPriceEntity> entities) {
        if (isEmpty(entities)) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список OpenPriceDTO в список OpenPriceEntity
     *
     * @param dtos список OpenPriceDTO для преобразования
     * @return список OpenPriceEntity или пустой список если dtos пустой или null
     */
    public List<OpenPriceEntity> toEntityList(List<OpenPriceDTO> dtos) {
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

