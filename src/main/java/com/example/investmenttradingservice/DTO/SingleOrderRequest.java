package com.example.investmenttradingservice.DTO;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для создания заявки для одного инструмента.
 * Уровни передаются как финальные цены (BigDecimal), без процентов.
 */
public record SingleOrderRequest(
        @NotBlank(message = "Идентификатор инструмента обязателен") String instrument,
        @NotNull(message = "Сумма является обязательной") BigDecimal amount,
        @NotBlank(message = "Направление торговли обязательно") String direction,
        @NotNull(message = "Время начала не может быть null") @JsonFormat(pattern = "HH:mm:ss") @JsonDeserialize(using = com.example.investmenttradingservice.util.LocalTimeOrNowDeserializer.class) LocalTime start_time,
        @NotNull(message = "Уровни обязательны") LevelsDTO levels) {
}
