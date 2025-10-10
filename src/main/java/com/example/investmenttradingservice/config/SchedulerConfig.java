package com.example.investmenttradingservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Конфигурация для Spring Scheduler.
 * 
 * Настройки:
 * - Включение планировщика (@EnableScheduling)
 * - Настройка пула потоков для выполнения задач
 * - Оптимизация для работы с большим количеством заявок
 * 
 * @author Investment Trading Service
 * @version 1.0
 */
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    /**
     * Настройка планировщика задач.
     * 
     * @param taskRegistrar регистратор задач
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

        // Настройка пула потоков для планировщика
        taskScheduler.setPoolSize(5); // 5 потоков для обработки заявок
        taskScheduler.setThreadNamePrefix("OrderScheduler-");
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setAwaitTerminationSeconds(30);

        // Инициализация планировщика
        taskScheduler.initialize();

        // Установка планировщика в регистратор
        taskRegistrar.setScheduler(taskScheduler);

        logger.info("Scheduler настроен: пул потоков = {}, префикс = {}",
                taskScheduler.getPoolSize(), "OrderScheduler-");
    }
}
