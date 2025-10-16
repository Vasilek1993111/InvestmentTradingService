package com.example.investmenttradingservice.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalTime;

/**
 * Десериализатор LocalTime, поддерживающий ключевое слово "now".
 *
 * <p>
 * Если входная строка равна "now" (без учета регистра), возвращается текущее
 * время в таймзоне Europe/Moscow, нормализованное до секунд.
 * Иначе применяется стандартный парсинг LocalTime (формат HH:mm:ss).
 * </p>
 */
public class LocalTimeOrNowDeserializer extends JsonDeserializer<LocalTime> {

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
        return LocalTime.parse(trimmed).withSecond(0).withNano(0);
    }
}
