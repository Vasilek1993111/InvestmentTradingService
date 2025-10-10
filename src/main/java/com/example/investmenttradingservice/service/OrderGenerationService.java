package com.example.investmenttradingservice.service;

import com.example.investmenttradingservice.DTO.GroupOrderRequest;
import com.example.investmenttradingservice.DTO.OrderDTO;
import com.example.investmenttradingservice.enums.OrderDirection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Сервис для генерации заявок на основе группового запроса.
 * Обрабатывает GroupOrderRequest и создает список одиночных заявок.
 */
@Service
public class OrderGenerationService {

    /** Логгер для записи операций сервиса */
    private static final Logger logger = LoggerFactory.getLogger(OrderGenerationService.class);

    /** Фасад для работы с инструментами */
    @Autowired
    private InstrumentServiceFacade instrumentServiceFacade;

    /** Идентификатор аккаунта из переменных окружения */
    @Value("${tinvest.account.id:}")
    private String accountId;

    /**
     * Генерирует список заявок на основе группового запроса.
     * 
     * @param request групповой запрос на создание заявок
     * @return список сгенерированных заявок
     */
    public List<OrderDTO> generateOrders(GroupOrderRequest request) {
        logger.info("Начало генерации заявок для {} инструментов", request.instruments().size());

        List<OrderDTO> orders = new ArrayList<>();

        try {
            // Обрабатываем каждый инструмент
            for (String instrumentId : request.instruments()) {
                List<OrderDTO> instrumentOrders = generateOrdersForInstrument(instrumentId, request);
                orders.addAll(instrumentOrders);

                logger.debug("Сгенерировано {} заявок для инструмента {}", instrumentOrders.size(), instrumentId);
            }

            logger.info("Всего сгенерировано {} заявок", orders.size());
            return orders;

        } catch (Exception e) {
            logger.error("Ошибка при генерации заявок: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Генерирует заявки для конкретного инструмента.
     * 
     * @param instrumentId идентификатор инструмента
     * @param request      групповой запрос
     * @return список заявок для инструмента
     */
    private List<OrderDTO> generateOrdersForInstrument(String instrumentId, GroupOrderRequest request) {
        List<OrderDTO> orders = new ArrayList<>();

        try {
            // Получаем данные о ценах для инструмента
            List<Object> priceData = instrumentServiceFacade.getPriceDataByType(
                    instrumentId, request.main_price().toLowerCase());

            if (priceData == null || priceData.isEmpty()) {
                logger.warn("Данные о ценах не найдены для инструмента: {}", instrumentId);
                return orders;
            }

            // Получаем цену инструмента
            BigDecimal instrumentPrice = instrumentServiceFacade.extractPriceFromData(priceData.get(0));
            if (instrumentPrice == null) {
                logger.warn("Не удалось извлечь цену инструмента: {}", instrumentId);
                return orders;
            }

            // Рассчитываем базовую цену за уровень
            BigDecimal priceForLevel = request.amount().divide(
                    new BigDecimal(request.levels().getLevelsCount()),
                    2,
                    java.math.RoundingMode.HALF_UP);

            // Генерируем заявки в зависимости от направления
            String direction = request.direction().toLowerCase();
            switch (direction) {
                case "buy" -> orders.addAll(generateOrdersForDirection(
                        request, instrumentId, priceForLevel, OrderDirection.ORDER_DIRECTION_BUY, instrumentPrice));
                case "sell" -> orders.addAll(generateOrdersForDirection(
                        request, instrumentId, priceForLevel, OrderDirection.ORDER_DIRECTION_SELL, instrumentPrice));
                case "all" -> {
                    // Для "all" создаем заявки на покупку и продажу
                    orders.addAll(generateOrdersForDirection(
                            request, instrumentId, priceForLevel, OrderDirection.ORDER_DIRECTION_BUY, instrumentPrice));
                    orders.addAll(generateOrdersForDirection(
                            request, instrumentId, priceForLevel, OrderDirection.ORDER_DIRECTION_SELL,
                            instrumentPrice));
                }
                default -> logger.warn("Неподдерживаемое направление торговли: {}", direction);
            }

        } catch (Exception e) {
            logger.error("Ошибка при генерации заявок для инструмента {}: {}", instrumentId, e.getMessage(), e);
        }

        return orders;
    }

    /**
     * Генерирует заявки для конкретного направления торговли.
     * 
     * @param request           исходный групповой запрос
     * @param instrumentId      идентификатор инструмента
     * @param basePriceForLevel базовая цена за уровень
     * @param direction         направление торговли
     * @param instrumentPrice   цена инструмента
     * @return список заявок для направления
     */
    private List<OrderDTO> generateOrdersForDirection(GroupOrderRequest request, String instrumentId,
            BigDecimal basePriceForLevel,
            OrderDirection direction, BigDecimal instrumentPrice) {
        List<OrderDTO> orders = new ArrayList<>();

        // Проходим по всем уровням
        for (int level = 1; level <= request.levels().getLevelsCount(); level++) {
            BigDecimal levelPercentage = request.levels().getLevelValue(level);

            if (levelPercentage == null) {
                logger.warn("Уровень {} не заполнен для инструмента: {}", level, instrumentId);
                continue;
            }

            // Рассчитываем цену с учетом процента и направления
            BigDecimal adjustedPrice = calculatePriceWithDirection(basePriceForLevel, levelPercentage, direction);

            // Рассчитываем лотность
            int lotSize = calculateLotSize(adjustedPrice, instrumentPrice);

            if (lotSize > 0) {
                // Создаем заявку с учетом времени исполнения
                OrderDTO order = OrderDTO.create(lotSize, adjustedPrice, direction, accountId, instrumentId,
                        request.start_time());
                orders.add(order);

                logger.debug(
                        "Создана заявка: инструмент={}, направление={}, уровень={}, цена={}, лотность={}, время={}",
                        instrumentId, direction.getValue(), level, adjustedPrice, lotSize, request.start_time());
            } else {
                logger.warn("Лотность для уровня {} равна нулю, заявка не создана", level);
            }
        }

        return orders;
    }

    /**
     * Рассчитывает цену с учетом процента и направления торговли.
     * 
     * @param basePrice  базовая цена
     * @param percentage процент уровня
     * @param direction  направление торговли
     * @return скорректированная цена
     */
    private BigDecimal calculatePriceWithDirection(BigDecimal basePrice, BigDecimal percentage,
            OrderDirection direction) {
        // Преобразуем процент в десятичную дробь
        BigDecimal decimalPercentage = percentage.divide(new BigDecimal("100"), 6, java.math.RoundingMode.HALF_UP);

        // Рассчитываем изменение цены
        BigDecimal priceChange = basePrice.multiply(decimalPercentage);

        return switch (direction) {
            case ORDER_DIRECTION_SELL -> {
                // Для продажи добавляем процент (цена выше)
                yield basePrice.add(priceChange).setScale(2, java.math.RoundingMode.HALF_UP);
            }
            case ORDER_DIRECTION_BUY -> {
                // Для покупки вычитаем процент (цена ниже)
                yield basePrice.subtract(priceChange).setScale(2, java.math.RoundingMode.HALF_UP);
            }
            default -> {
                logger.warn("Неподдерживаемое направление торговли: {}", direction);
                yield basePrice;
            }
        };
    }

    /**
     * Рассчитывает лотность заявки.
     * 
     * @param adjustedPrice   скорректированная цена
     * @param instrumentPrice цена инструмента
     * @return лотность (округленная в меньшую сторону)
     */
    private int calculateLotSize(BigDecimal adjustedPrice, BigDecimal instrumentPrice) {
        if (instrumentPrice.compareTo(BigDecimal.ZERO) == 0) {
            logger.warn("Цена инструмента равна нулю, невозможно рассчитать лотность");
            return 0;
        }

        // Рассчитываем лотность: adjustedPrice / instrumentPrice
        BigDecimal lotRatio = adjustedPrice.divide(instrumentPrice, 10, java.math.RoundingMode.HALF_UP);
        int lotSize = lotRatio.setScale(0, java.math.RoundingMode.FLOOR).intValue();

        return Math.max(0, lotSize); // Гарантируем, что лотность не отрицательная
    }
}
