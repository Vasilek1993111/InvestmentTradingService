
package com.example.investmenttradingservice.util;

import java.time.ZoneId;

/**
 * Утилитный класс для работы с временными зонами
 * Всегда использует таймзону UTC+3 (Europe/Moscow)
 */
public class TimeZoneUtils {

    /**
     * Константа для московской временной зоны (UTC+3)
     */
    public static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    /**
     * Получить московскую временную зону
     * 
     * @return ZoneId для Europe/Moscow
     */
    public static ZoneId getMoscowZone() {
        return MOSCOW_ZONE;
    }
}
