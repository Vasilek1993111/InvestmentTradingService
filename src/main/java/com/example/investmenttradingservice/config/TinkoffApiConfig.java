package com.example.investmenttradingservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OrdersService;

/**
 * TinkoffApiConfig — конфигурация бинов официального Java SDK T-Invest.
 *
 * <p>
 * Регистрирует {@link InvestApi} и {@link OrdersService} для инъекции в
 * сервисы.
 * Поддерживает работу как с реальным рынком, так и с песочницей в зависимости
 * от
 * флага настроек.
 * </p>
 */
@Configuration
public class TinkoffApiConfig {

    private static final Logger logger = LoggerFactory.getLogger(TinkoffApiConfig.class);

    @Value("${tinvest.api.token}")
    private String apiToken;

    /**
     * Создает бин {@link InvestApi} на основе токена и режима работы.
     *
     * @return инициализированный клиент InvestApi
     */
    @Bean
    public InvestApi investApi() {
        if (apiToken == null || apiToken.isEmpty()) {
            throw new IllegalStateException("T-Invest API token is not configured (property 'tinvest.api.token').");
        }
        
        return InvestApi.create(apiToken);
    }

    /**
     * Создает бин {@link OrdersService} из {@link InvestApi}.
     *
     * @param investApi клиент InvestApi
     * @return сервис заявок OrdersService
     */
    @Bean
    public OrdersService ordersService(InvestApi investApi) {
        return investApi.getOrdersService();
    }

    @Bean
    public MarketDataService marketDataService(InvestApi investApi) {
        return investApi.getMarketDataService();
    }

}
