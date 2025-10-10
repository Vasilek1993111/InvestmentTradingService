package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.DividendDto;
import com.example.investmenttradingservice.entity.DividendEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования DividendEntity в DividendDto и обратно
 * 
 * <p>
 * Обеспечивает конвертацию между сущностями дивидендов и их DTO
 * представлениями.
 * Включает методы для работы с единичными объектами и списками.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Component
public class DividendMapper {

    /**
     * Преобразует DividendEntity в DividendDto
     *
     * @param entity DividendEntity для преобразования
     * @return DividendDto или null если entity равен null
     */
    public DividendDto toDTO(DividendEntity entity) {
        if (entity == null) {
            return null;
        }

        return new DividendDto(
                entity.getFigi(),
                entity.getDeclaredDate(),
                entity.getRecordDate(),
                entity.getPaymentDate(),
                entity.getDividendValue(),
                entity.getCurrency(),
                entity.getDividendType());
    }

    /**
     * Преобразует DividendDto в DividendEntity
     *
     * @param dto DividendDto для преобразования
     * @return DividendEntity или null если dto равен null
     */
    public DividendEntity toEntity(DividendDto dto) {
        if (dto == null) {
            return null;
        }

        return new DividendEntity(
                dto.figi(),
                dto.declaredDate(),
                dto.recordDate(),
                dto.paymentDate(),
                dto.dividendValue(),
                dto.currency(),
                dto.dividendType());
    }

    /**
     * Преобразует список DividendEntity в список DividendDto
     *
     * @param entities список DividendEntity для преобразования
     * @return список DividendDto или пустой список если entities пустой или null
     */
    public List<DividendDto> toDTOList(List<DividendEntity> entities) {
        if (isEmpty(entities)) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список DividendDto в список DividendEntity
     *
     * @param dtos список DividendDto для преобразования
     * @return список DividendEntity или пустой список если dtos пустой или null
     */
    public List<DividendEntity> toEntityList(List<DividendDto> dtos) {
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

