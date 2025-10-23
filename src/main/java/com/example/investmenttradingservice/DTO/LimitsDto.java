package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;

/**
 * LimitsDto — границы изменения цены инструмента за торговую сессию.
 *
 * <p>
 * Представляет верхнюю и нижнюю границы цены для инструмента, полученные из
 * OrderBook T-Invest API. Используется для валидации заявок и риск-менеджмента.
 * </p>
 *
 * @param instrumentId FIGI/InstrumentId инструмента, для которого получены
 *                     лимиты
 * @param limitDown    нижняя граница цены (может быть null, если нет в ответе)
 * @param limitUp      верхняя граница цены (может быть null, если нет в ответе)
 */
public record LimitsDto(
        String instrumentId,
        BigDecimal limitDown,
        BigDecimal limitUp) {
}
