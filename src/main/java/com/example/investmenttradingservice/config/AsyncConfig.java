package com.example.investmenttradingservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Конфигурация асинхронной обработки для высокопроизводительной отправки заявок
 * в T-Invest API.
 * 
 * <p>
 * Настройки оптимизированы для:
 * </p>
 * <ul>
 * <li>Быстрой отправки заявок с минимальной задержкой</li>
 * <li>Обработки высоких нагрузок (1000+ RPS)</li>
 * <li>Thread-safe операций</li>
 * <li>Graceful shutdown при завершении работы</li>
 * </ul>
 * 
 * <p>
 * Используется ThreadPoolTaskExecutor с настройками:
 * </p>
 * <ul>
 * <li>Core pool size: количество ядер CPU * 2</li>
 * <li>Max pool size: количество ядер CPU * 4</li>
 * <li>Queue capacity: 1000 задач в очереди</li>
 * <li>Rejection policy: CallerRunsPolicy (выполнение в вызывающем потоке при
 * переполнении)</li>
 * </ul>
 * 
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${app.async.core-pool-size:#{T(Runtime).getRuntime().availableProcessors() * 2}}")
    private int corePoolSize;

    @Value("${app.async.max-pool-size:#{T(Runtime).getRuntime().availableProcessors() * 4}}")
    private int maxPoolSize;

    @Value("${app.async.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${app.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /**
     * Основной ThreadPool для асинхронной отправки заявок в T-Invest API.
     * 
     * <p>
     * Оптимизирован для:
     * </p>
     * <ul>
     * <li>Быстрой обработки I/O операций (API вызовы)</li>
     * <li>Минимальной задержки при отправке заявок</li>
     * <li>Высокой пропускной способности</li>
     * </ul>
     * 
     * @return настроенный Executor для асинхронных операций
     */
    @Bean(name = "orderProcessingExecutor")
    public Executor orderProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Основные настройки пула потоков
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);

        // Настройки именования потоков для удобства мониторинга
        executor.setThreadNamePrefix("OrderProcessing-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // Политика обработки переполнения очереди
        // CallerRunsPolicy обеспечивает обратную связь и предотвращает потерю задач
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Инициализация пула потоков
        executor.initialize();

        logger.info("Инициализирован ThreadPool для обработки заявок: core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }

    /**
     * Специализированный ThreadPool для критически важных операций T-Invest API.
     * 
     * <p>
     * Используется для:
     * </p>
     * <ul>
     * <li>Отправки заявок в реальном времени</li>
     * <li>Отмены заявок</li>
     * <li>Получения статусов заявок</li>
     * </ul>
     * 
     * <p>
     * Настройки оптимизированы для минимальной задержки:
     * </p>
     * <ul>
     * <li>Меньший размер очереди для быстрого отклика</li>
     * <li>Быстрое создание новых потоков при необходимости</li>
     * <li>Приоритетная обработка критических операций</li>
     * </ul>
     * 
     * @return настроенный Executor для критических операций
     */
    @Bean(name = "criticalOperationsExecutor")
    public Executor criticalOperationsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Настройки для минимальной задержки
        executor.setCorePoolSize(corePoolSize / 2); // Меньше базового пула
        executor.setMaxPoolSize(maxPoolSize); // Но может расшириться до максимума
        executor.setQueueCapacity(100); // Маленькая очередь для быстрого отклика
        executor.setKeepAliveSeconds(30); // Быстрое освобождение неиспользуемых потоков

        executor.setThreadNamePrefix("CriticalOps-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);

        // AbortPolicy для критических операций - лучше уведомить об ошибке, чем ждать
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        executor.initialize();

        logger.info("Инициализирован ThreadPool для критических операций: core={}, max={}, queue={}",
                corePoolSize / 2, maxPoolSize, 100);

        return executor;
    }

    /**
     * ThreadPool для фоновых операций (мониторинг, статистика, очистка).
     * 
     * <p>
     * Используется для:
     * </p>
     * <ul>
     * <li>Периодической синхронизации с T-Invest API</li>
     * <li>Обновления статусов заявок</li>
     * <li>Сбора метрик и статистики</li>
     * <li>Очистки устаревших данных</li>
     * </ul>
     * 
     * @return настроенный Executor для фоновых операций
     */
    @Bean(name = "backgroundTasksExecutor")
    public Executor backgroundTasksExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Настройки для фоновых задач
        executor.setCorePoolSize(2); // Минимальный пул для фоновых задач
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(120);

        executor.setThreadNamePrefix("Background-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // DiscardOldestPolicy для фоновых задач - можно потерять старые задачи
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());

        executor.initialize();

        logger.info("Инициализирован ThreadPool для фоновых операций: core=2, max=4, queue=500");

        return executor;
    }
}
