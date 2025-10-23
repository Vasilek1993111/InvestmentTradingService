package com.example.investmenttradingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OrdersService;

/**
 * Конфигурация бинов официального Java SDK T-Invest API.
 * 
 * <p>
 * Предоставляет централизованную конфигурацию для работы с T-Invest API через
 * официальный Java SDK. Регистрирует основные сервисы для торговых операций
 * и получения рыночных данных.
 * </p>
 * 
 * <p>
 * Основные компоненты:
 * </p>
 * <ul>
 * <li>{@link InvestApi} - основной клиент для работы с API</li>
 * <li>{@link OrdersService} - сервис для управления заявками</li>
 * <li>{@link MarketDataService} - сервис для получения рыночных данных</li>
 * </ul>
 * 
 * <p>
 * Конфигурация поддерживает:
 * </p>
 * <ul>
 * <li>Работу с реальным рынком и песочницей</li>
 * <li>Thread-safe операции</li>
 * <li>Автоматическое управление соединениями</li>
 * <li>Retry-механику при временных сбоях</li>
 * </ul>
 * 
 * @author Investment Trading Service
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class TinkoffApiConfig {

    /**
     * API токен для аутентификации в T-Invest API.
     * 
     * <p>
     * Токен должен быть получен в личном кабинете T-Invest и настроен
     * через переменную окружения или application.properties:
     * </p>
     * <pre>
     * tinvest.api.token=your_api_token_here
     * </pre>
     * 
     * <p>
     * Токен используется для:
     * </p>
     * <ul>
     * <li>Аутентификации всех запросов к API</li>
     * <li>Идентификации аккаунта для торговых операций</li>
     * <li>Получения доступа к рыночным данным</li>
     * </ul>
     */
    @Value("${tinvest.api.token}")
    private String apiToken;

    /**
     * Создает и настраивает основной клиент {@link InvestApi} для работы с T-Invest API.
     * 
     * <p>
     * Метод выполняет следующие действия:
     * </p>
     * <ul>
     * <li>Проверяет наличие и валидность API токена</li>
     * <li>Создает экземпляр InvestApi с переданным токеном</li>
     * <li>Настраивает соединение с T-Invest API</li>
     * </ul>
     * 
     * <p>
     * Созданный клиент поддерживает:
     * </p>
     * <ul>
     * <li>Thread-safe операции</li>
     * <li>Автоматическое управление соединениями</li>
     * <li>Retry-механику при временных сбоях</li>
     * <li>Логирование всех API вызовов</li>
     * </ul>
     * 
     * @return инициализированный клиент InvestApi для работы с T-Invest API
     * @throws IllegalStateException если API токен не настроен или пуст
     * 
     * @see InvestApi#create(String)
     */
    @Bean
    public InvestApi investApi() {
        if (apiToken == null || apiToken.isEmpty()) {
            throw new IllegalStateException("T-Invest API token is not configured (property 'tinvest.api.token').");
        }
        
        return InvestApi.create(apiToken);
    }

    /**
     * Создает сервис для управления заявками {@link OrdersService}.
     * 
     * <p>
     * OrdersService предоставляет методы для:
     * </p>
     * <ul>
     * <li>Создания и отправки заявок (PostOrder)</li>
     * <li>Получения статуса заявок</li>
     * <li>Отмены заявок</li>
     * <li>Получения истории заявок</li>
     * </ul>
     * 
     * <p>
     * Сервис автоматически использует настройки из основного клиента InvestApi,
     * включая токен аутентификации и параметры соединения.
     * </p>
     * 
     * @param investApi основной клиент T-Invest API
     * @return настроенный сервис для управления заявками
     * 
     * @see InvestApi#getOrdersService()
     */
    @Bean
    public OrdersService ordersService(InvestApi investApi) {
        return investApi.getOrdersService();
    }

    /**
     * Создает сервис для получения рыночных данных {@link MarketDataService}.
     * 
     * <p>
     * MarketDataService предоставляет методы для:
     * </p>
     * <ul>
     * <li>Получения котировок инструментов</li>
     * <li>Получения стакана заявок (OrderBook)</li>
     * <li>Получения исторических данных</li>
     * <li>Получения информации о лимитах торговли</li>
     * </ul>
     * 
     * <p>
     * Сервис используется для:
     * </p>
     * <ul>
     * <li>Получения текущих цен инструментов</li>
     * <li>Проверки лимитов торговли (limitUp/limitDown)</li>
     * <li>Анализа рыночной ситуации</li>
     * </ul>
     * 
     * @param investApi основной клиент T-Invest API
     * @return настроенный сервис для получения рыночных данных
     * 
     * @see InvestApi#getMarketDataService()
     */
    @Bean
    public MarketDataService marketDataService(InvestApi investApi) {
        return investApi.getMarketDataService();
    }

}
