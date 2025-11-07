package com.example.investmenttradingservice.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Десериализатор LocalTime, поддерживающий ключевое слово "now".
 *
 * <p>
 * Если входная строка равна "now" (без учета регистра), возвращается текущее
 * время в таймзоне Europe/Moscow, нормализованное до секунд.
 * Иначе применяется парсинг LocalTime с поддержкой форматов:
 * - HH:mm:ss (с секундами)
 * - HH:mm (без секунд, секунды устанавливаются в 0)
 * </p>
 */
public class LocalTimeOrNowDeserializer extends JsonDeserializer<LocalTime> {

    private static final DateTimeFormatter FORMATTER_WITH_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_WITHOUT_SECONDS = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.equalsIgnoreCase("now")) {
            return LocalTime.now(TimeZoneUtils.getMoscowZone()).withSecond(0).withNano(0);
        }
        
        // Пробуем парсить с секундами (формат HH:mm:ss)
        try {
            LocalTime time = LocalTime.parse(trimmed, FORMATTER_WITH_SECONDS);
            return time.withNano(0);
        } catch (DateTimeParseException e) {
            // Если не получилось, пробуем без секунд (формат HH:mm)
            try {
                LocalTime time = LocalTime.parse(trimmed, FORMATTER_WITHOUT_SECONDS);
                return time.withSecond(0).withNano(0);
            } catch (DateTimeParseException e2) {
                // Если и это не сработало, пробуем стандартный парсер ISO-8601
                try {
                    return LocalTime.parse(trimmed).withNano(0);
                } catch (DateTimeParseException e3) {
                    throw new IOException(
                            String.format("Не удалось распарсить время '%s'. Ожидается формат HH:mm:ss или HH:mm", trimmed),
                            e3);
                }
            }
        }
    }
}
