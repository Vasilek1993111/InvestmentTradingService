package com.example.investmenttradingservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.investmenttradingservice.util.TimeZoneUtils;
import com.example.investmenttradingservice.util.WorkingDaysUtils;

import java.time.LocalDateTime;

/**
 * Сервис для автоматического обновления кэша по расписанию
 *
 * <p>
 * Этот сервис обеспечивает автоматическое обновление данных в кэше
 * согласно расписанию торговых сессий:
 * </p>
 * <ul>
 * <li>По рабочим дням: обновление в 03:00 по МСК</li>
 * <li>По выходным дням: обновление в 01:50 по МСК</li>
 * </ul>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Автоматическое обновление кэша акций</li>
 * <li>Автоматическое обновление кэша фьючерсов</li>
 * <li>Автоматическое обновление кэша индикативов</li>
 * <li>Автоматическое обновление кэша цен закрытия</li>
 * <li>Автоматическое обновление кэша цен открытия</li>
 * <li>Автоматическое обновление кэша цен закрытия вечерней сессии</li>
 * <li>Логирование всех операций обновления</li>
 * <li>Обработка ошибок при обновлении</li>
 * </ul>
 *
 * <p>
 * Расписание обновлений:
 * </p>
 * <ul>
 * <li>Рабочие дни (понедельник-пятница): 03:00 МСК</li>
 * <li>Выходные дни (суббота-воскресенье): 01:50 МСК</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class CacheSchedulerService {

    /** Логгер для записи операций планировщика */
    private static final Logger logger = LoggerFactory.getLogger(CacheSchedulerService.class);

    /** Сервис для работы с кэшем */
    private final CacheService cacheService;

    /**
     * Конструктор сервиса планировщика кэша
     *
     * @param cacheService сервис для работы с кэшем
     */
    @Autowired
    public CacheSchedulerService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Автоматическое обновление кэша по рабочим дням
     *
     * <p>
     * Выполняется каждый рабочий день (понедельник-пятница) в 03:00 по московскому
     * времени.
     * Обновляет все кэши для подготовки к торговой сессии.
     * </p>
     *
     * <p>
     * Cron выражение: "0 0 3 * * MON-FRI"
     * </p>
     * <ul>
     * <li>0 - секунды (0)</li>
     * <li>0 - минуты (0)</li>
     * <li>3 - часы (3)</li>
     * <li>* - день месяца (любой)</li>
     * <li>* - месяц (любой)</li>
     * <li>MON-FRI - день недели (понедельник-пятница)</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 3 * * MON-FRI", zone = "Europe/Moscow")
    public void updateCacheOnWorkingDays() {
        String taskId = "WORKING_DAYS_UPDATE_" + LocalDateTime.now(TimeZoneUtils.getMoscowZone());
        logger.info("[{}] Начало автоматического обновления кэша по рабочим дням", taskId);

        try {
            // Проверяем, что сегодня действительно рабочий день
            if (WorkingDaysUtils.isWorkingDay(LocalDateTime.now(TimeZoneUtils.getMoscowZone()).toLocalDate())) {
                cacheService.manualWarmupCache();
                logger.info("[{}] Автоматическое обновление кэша по рабочим дням завершено успешно", taskId);
            } else {
                logger.info("[{}] Сегодня не рабочий день, обновление кэша пропущено", taskId);
            }
        } catch (Exception e) {
            logger.error("[{}] Ошибка при автоматическом обновлении кэша по рабочим дням: {}",
                    taskId, e.getMessage(), e);
        }
    }

    /**
     * Автоматическое обновление кэша по выходным дням
     *
     * <p>
     * Выполняется каждый выходной день (суббота-воскресенье) в 01:50 по московскому
     * времени.
     * Обновляет все кэши для подготовки к следующей торговой неделе.
     * </p>
     *
     * <p>
     * Cron выражение: "0 50 1 * * SAT,SUN"
     * </p>
     * <ul>
     * <li>0 - секунды (0)</li>
     * <li>50 - минуты (50)</li>
     * <li>1 - часы (1)</li>
     * <li>* - день месяца (любой)</li>
     * <li>* - месяц (любой)</li>
     * <li>SAT,SUN - день недели (суббота-воскресенье)</li>
     * </ul>
     */
    @Scheduled(cron = "0 50 1 * * SAT,SUN", zone = "Europe/Moscow")
    public void updateCacheOnWeekends() {
        String taskId = "WEEKEND_UPDATE_" + LocalDateTime.now(TimeZoneUtils.getMoscowZone());
        logger.info("[{}] Начало автоматического обновления кэша по выходным дням", taskId);

        try {
            // Проверяем, что сегодня действительно выходной день
            if (!WorkingDaysUtils.isWorkingDay(LocalDateTime.now(TimeZoneUtils.getMoscowZone()).toLocalDate())) {
                cacheService.manualWarmupCache();
                logger.info("[{}] Автоматическое обновление кэша по выходным дням завершено успешно", taskId);
            } else {
                logger.info("[{}] Сегодня рабочий день, обновление кэша пропущено", taskId);
            }
        } catch (Exception e) {
            logger.error("[{}] Ошибка при автоматическом обновлении кэша по выходным дням: {}",
                    taskId, e.getMessage(), e);
        }
    }

    /**
     * Ежедневная проверка состояния кэша
     *
     * <p>
     * Выполняется каждый день в 02:30 по московскому времени для мониторинга
     * состояния кэша и логирования статистики.
     * </p>
     *
     * <p>
     * Cron выражение: "0 30 2 * * *"
     * </p>
     * <ul>
     * <li>0 - секунды (0)</li>
     * <li>30 - минуты (30)</li>
     * <li>2 - часы (2)</li>
     * <li>* - день месяца (любой)</li>
     * <li>* - месяц (любой)</li>
     * <li>* - день недели (любой)</li>
     * </ul>
     */
    @Scheduled(cron = "0 30 2 * * *", zone = "Europe/Moscow")
    public void dailyCacheHealthCheck() {
        String taskId = "DAILY_HEALTH_CHECK_" + LocalDateTime.now(TimeZoneUtils.getMoscowZone());
        logger.info("[{}] Начало ежедневной проверки состояния кэша", taskId);

        try {
            var cacheStats = cacheService.getCacheStats();

            logger.info("[{}] Статистика кэша:", taskId);
            cacheStats.forEach((cacheName, size) -> {
                logger.info("[{}] Кэш {}: {} записей", taskId, cacheName, size);
            });

            // Проверяем, что все кэши не пустые
            boolean hasEmptyCaches = cacheStats.values().stream()
                    .anyMatch(size -> size instanceof Integer && (Integer) size == 0);

            if (hasEmptyCaches) {
                logger.warn("[{}] Обнаружены пустые кэши, рекомендуется ручное обновление", taskId);
            } else {
                logger.info("[{}] Все кэши содержат данные, состояние нормальное", taskId);
            }

            logger.info("[{}] Ежедневная проверка состояния кэша завершена", taskId);

        } catch (Exception e) {
            logger.error("[{}] Ошибка при ежедневной проверке состояния кэша: {}",
                    taskId, e.getMessage(), e);
        }
    }

}
