package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.LastPriceDTO;
import com.example.investmenttradingservice.entity.LastPriceEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования LastPriceEntity в LastPriceDTO и обратно
 * 
 * <p>
 * Обеспечивает конвертацию между сущностями последних цен и их DTO
 * представлениями.
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
public class LastPriceMapper {

    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    /**
     * Преобразует LastPriceEntity в LastPriceDTO
     *
     * @param entity LastPriceEntity для преобразования
     * @return LastPriceDTO или null если entity равен null
     */
    public LastPriceDTO toDTO(LastPriceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new LastPriceDTO(
                entity.getId().getFigi(),
                null, // direction - не хранится в Entity
                entity.getPrice(),
                0L, // quantity - не хранится в Entity
                entity.getId().getTime().atZone(MOSCOW_ZONE).toInstant(),
                null // tradeSource - не хранится в Entity
        );
    }

    /**
     * Преобразует LastPriceDTO в LastPriceEntity
     *
     * @param dto LastPriceDTO для преобразования
     * @return LastPriceEntity или null если dto равен null
     */
    public LastPriceEntity toEntity(LastPriceDTO dto) {
        if (dto == null) {
            return null;
        }

        LocalDateTime time = LocalDateTime.ofInstant(dto.time(), MOSCOW_ZONE);
        return new LastPriceEntity(
                dto.figi(),
                time,
                dto.price(),
                null, // currency - не передается в DTO
                null // exchange - не передается в DTO
        );
    }

    /**
     * Преобразует список LastPriceEntity в список LastPriceDTO
     *
     * @param entities список LastPriceEntity для преобразования
     * @return список LastPriceDTO или пустой список если entities пустой или null
     */
    public List<LastPriceDTO> toDTOList(List<LastPriceEntity> entities) {
        if (isEmpty(entities)) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список LastPriceDTO в список LastPriceEntity
     *
     * @param dtos список LastPriceDTO для преобразования
     * @return список LastPriceEntity или пустой список если dtos пустой или null
     */
    public List<LastPriceEntity> toEntityList(List<LastPriceDTO> dtos) {
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

