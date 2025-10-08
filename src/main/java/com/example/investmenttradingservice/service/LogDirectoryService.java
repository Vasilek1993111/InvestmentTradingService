package com.example.investmenttradingservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Сервис для создания папок логов с датами при запуске приложения
 *
 * <p>
 * Этот сервис автоматически создает папку с текущей датой в директории logs
 * при запуске приложения. Это обеспечивает правильную организацию логов
 * по папкам дат для удобного мониторинга и анализа.
 * </p>
 *
 * <p>
 * Основные функции:
 * </p>
 * <ul>
 * <li>Автоматическое создание папки с текущей датой при запуске</li>
 * <li>Проверка существования папки logs</li>
 * <li>Логирование процесса создания папок</li>
 * </ul>
 *
 * <p>
 * Структура папок:
 * </p>
 * <ul>
 * <li>logs/ - основная папка логов</li>
 * <li>logs/YYYY-MM-DD/ - папка для логов конкретной даты</li>
 * </ul>
 *
 * @author Investment Trading Service Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class LogDirectoryService {

    /** Логгер для записи операций с папками логов */
    private static final Logger logger = LoggerFactory.getLogger(LogDirectoryService.class);

    /** Путь к основной папке логов */
    private static final String LOGS_BASE_DIR = "logs";

    /** Формат даты для папок */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Автоматическое создание папки с текущей датой при запуске приложения
     *
     * <p>
     * Выполняется после полной инициализации Spring контекста.
     * Создает папку с текущей датой в формате YYYY-MM-DD для организации логов.
     * </p>
     *
     * <p>
     * Процесс выполнения:
     * </p>
     * <ul>
     * <li>Проверка существования основной папки logs</li>
     * <li>Создание основной папки logs при необходимости</li>
     * <li>Создание папки с текущей датой</li>
     * <li>Логирование результатов операции</li>
     * </ul>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createLogDirectoryOnStartup() {
        try {
            // Создаем основную папку logs если её нет
            File logsBaseDir = new File(LOGS_BASE_DIR);
            if (!logsBaseDir.exists()) {
                boolean created = logsBaseDir.mkdirs();
                if (created) {
                    logger.info("Создана основная папка логов: {}", LOGS_BASE_DIR);
                } else {
                    logger.warn("Не удалось создать основную папку логов: {}", LOGS_BASE_DIR);
                }
            }

            // Создаем папку с текущей датой
            String currentDate = LocalDate.now().format(DATE_FORMATTER);
            File dateDir = new File(logsBaseDir, currentDate);

            if (!dateDir.exists()) {
                boolean created = dateDir.mkdirs();
                if (created) {
                    logger.info("Создана папка логов для даты: {}/{}", LOGS_BASE_DIR, currentDate);
                } else {
                    logger.warn("Не удалось создать папку логов для даты: {}/{}", LOGS_BASE_DIR, currentDate);
                }
            } else {
                logger.debug("Папка логов для даты уже существует: {}/{}", LOGS_BASE_DIR, currentDate);
            }

        } catch (Exception e) {
            logger.error("Ошибка при создании папки логов: {}", e.getMessage(), e);
        }
    }

    /**
     * Получает путь к папке логов для текущей даты
     *
     * <p>
     * Возвращает полный путь к папке логов для текущей даты.
     * Используется другими сервисами для определения места сохранения логов.
     * </p>
     *
     * @return путь к папке логов для текущей даты
     */
    public String getCurrentDateLogDirectory() {
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        return LOGS_BASE_DIR + File.separator + currentDate;
    }

    /**
     * Получает путь к папке логов для указанной даты
     *
     * <p>
     * Возвращает полный путь к папке логов для указанной даты.
     * Используется для доступа к историческим логам.
     * </p>
     *
     * @param date дата для которой нужно получить путь к папке логов
     * @return путь к папке логов для указанной даты
     */
    public String getLogDirectoryForDate(LocalDate date) {
        String dateString = date.format(DATE_FORMATTER);
        return LOGS_BASE_DIR + File.separator + dateString;
    }

    /**
     * Проверяет существование папки логов для текущей даты
     *
     * <p>
     * Проверяет, существует ли папка логов для текущей даты.
     * Используется для валидации перед операциями с логами.
     * </p>
     *
     * @return true если папка существует, false в противном случае
     */
    public boolean isCurrentDateLogDirectoryExists() {
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        File dateDir = new File(LOGS_BASE_DIR, currentDate);
        return dateDir.exists() && dateDir.isDirectory();
    }
}

