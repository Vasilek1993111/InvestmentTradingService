package com.example.investmenttradingservice.mapper;

import com.example.investmenttradingservice.DTO.IndicativeDTO;
import com.example.investmenttradingservice.entity.IndicativeEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования IndicativeEntity в IndicativeDTO и обратно
 * 
 * <p>
 * Обеспечивает конвертацию между сущностями индикативных инструментов и их DTO
 * представлениями.
 * Включает методы для работы с единичными объектами и списками.
 * </p>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Component
public class IndicativeMapper {

    /**
     * Преобразует IndicativeEntity в IndicativeDTO
     *
     * @param entity IndicativeEntity для преобразования
     * @return IndicativeDTO или null если entity равен null
     */
    public IndicativeDTO toDTO(IndicativeEntity entity) {
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
     * Преобразует IndicativeDTO в IndicativeEntity
     *
     * @param dto IndicativeDTO для преобразования
     * @return IndicativeEntity или null если dto равен null
     */
    public IndicativeEntity toEntity(IndicativeDTO dto) {
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
     * Преобразует список IndicativeEntity в список IndicativeDTO
     *
     * @param entities список IndicativeEntity для преобразования
     * @return список IndicativeDTO или пустой список если entities пустой или null
     */
    public List<IndicativeDTO> toDTOList(List<IndicativeEntity> entities) {
        if (isEmpty(entities)) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список IndicativeDTO в список IndicativeEntity
     *
     * @param dtos список IndicativeDTO для преобразования
     * @return список IndicativeEntity или пустой список если dtos пустой или null
     */
    public List<IndicativeEntity> toEntityList(List<IndicativeDTO> dtos) {
        if (isEmpty(dtos)) {
            return List.of();
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

