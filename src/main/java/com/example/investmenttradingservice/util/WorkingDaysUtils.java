package com.example.investmenttradingservice.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилита для работы с рабочими днями
 *
 * <p>
 * Предоставляет методы для определения предыдущих рабочих дней с учетом
 * выходных дней (суббота и воскресенье). Используется для получения цен
 * закрытия за предыдущий торговый день.
 * </p>
 *
 * <p>
 * Логика работы:
 * </p>
 * <ul>
 * <li>Пятница → четверг (предыдущий день)</li>
 * <li>Суббота → пятница (последний рабочий день недели)</li>
 * <li>Воскресенье → пятница (последний рабочий день недели)</li>
 * <li>Понедельник → пятница (последний рабочий день предыдущей недели)</li>
 * <li>Вторник → понедельник (предыдущий день)</li>
 * <li>Среда → вторник (предыдущий день)</li>
 * <li>Четверг → среда (предыдущий день)</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
public class WorkingDaysUtils {

    /**
     * Получает предыдущий рабочий день для заданной даты
     *
     * <p>
     * Определяет предыдущий торговый день с учетом выходных.
     * Если текущий день - понедельник, возвращает пятницу предыдущей недели.
     * Если текущий день - суббота или воскресенье, возвращает пятницу текущей
     * недели.
     * </p>
     *
     * @param currentDate текущая дата
     * @return предыдущий рабочий день
     */
    public static LocalDate getPreviousWorkingDay(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }

        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

        return switch (dayOfWeek) {
            case MONDAY -> currentDate.minus(3, ChronoUnit.DAYS); // Понедельник → пятница
            case TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> currentDate.minus(1, ChronoUnit.DAYS); // Вторник-пятница →
                                                                                                // предыдущий день
            case SATURDAY, SUNDAY -> getLastFridayOfWeek(currentDate); // Суббота/воскресенье → пятница
        };
    }

    /**
     * Получает последнюю пятницу недели для заданной даты
     *
     * <p>
     * Если дата приходится на субботу или воскресенье,
     * возвращает пятницу той же недели.
     * </p>
     *
     * @param date дата для поиска пятницы
     * @return пятница той же недели
     */
    private static LocalDate getLastFridayOfWeek(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int daysToSubtract = switch (dayOfWeek) {
            case SATURDAY -> 1; // Суббота → пятница (1 день назад)
            case SUNDAY -> 2; // Воскресенье → пятница (2 дня назад)
            default -> throw new IllegalArgumentException("Метод должен вызываться только для субботы и воскресенья");
        };
        return date.minus(daysToSubtract, ChronoUnit.DAYS);
    }

    /**
     * Получает список предыдущих рабочих дней для поиска цен
     *
     * <p>
     * Возвращает список дат для поиска цен закрытия, начиная с предыдущего
     * рабочего дня. Используется для fallback механизма при отсутствии
     * данных за конкретный день.
     * </p>
     *
     * <p>
     * Алгоритм:
     * </p>
     * <ul>
     * <li>Начинаем с предыдущего рабочего дня</li>
     * <li>Добавляем предыдущие дни до максимального количества попыток</li>
     * <li>Пропускаем выходные дни</li>
     * </ul>
     *
     * @param currentDate текущая дата
     * @param maxDays     максимальное количество дней для поиска
     * @return список дат для поиска цен
     */
    public static List<LocalDate> getPreviousWorkingDaysForSearch(LocalDate currentDate, int maxDays) {
        if (currentDate == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }
        if (maxDays <= 0) {
            throw new IllegalArgumentException("Максимальное количество дней должно быть больше 0");
        }

        List<LocalDate> workingDays = new ArrayList<>();
        LocalDate searchDate = getPreviousWorkingDay(currentDate);

        for (int i = 0; i < maxDays && searchDate != null; i++) {
            workingDays.add(searchDate);
            searchDate = getPreviousWorkingDay(searchDate);
        }

        return workingDays;
    }

    /**
     * Проверяет, является ли дата рабочим днем
     *
     * <p>
     * Рабочими днями считаются понедельник-пятница.
     * Суббота и воскресенье - выходные дни.
     * </p>
     *
     * @param date дата для проверки
     * @return true, если дата является рабочим днем
     */
    public static boolean isWorkingDay(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * Получает количество рабочих дней между двумя датами
     *
     * <p>
     * Подсчитывает количество рабочих дней (понедельник-пятница)
     * между двумя датами включительно.
     * </p>
     *
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @return количество рабочих дней
     */
    public static long getWorkingDaysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты не могут быть null");
        }

        if (startDate.isAfter(endDate)) {
            return 0;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long weekends = 0;

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!isWorkingDay(current)) {
                weekends++;
            }
            current = current.plusDays(1);
        }

        return totalDays - weekends;
    }
}
