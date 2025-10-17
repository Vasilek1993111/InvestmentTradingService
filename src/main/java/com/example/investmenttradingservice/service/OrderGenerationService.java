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
    @Autowired(required = false)
    private InstrumentServiceFacade instrumentServiceFacade;

    /** Сервис для работы с T-Invest API */
    @Autowired(required = false)
    private TInvestApiService tInvestApiService;

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

    // Удалено: generateOrdersWithDirectLevelPrices - заменён на
    // generateSingleInstrumentWithDirectLevelPrices

    /**
     * Генерирует заявки для одного инструмента, когда уровни — это финальные цены.
     * Использует ту же логику распределения суммы и расчёта лотности.
     */
    public List<OrderDTO> generateSingleInstrumentWithDirectLevelPrices(String instrumentId, String directionRaw,
            java.math.BigDecimal amount, com.example.investmenttradingservice.DTO.LevelsDTO levels,
            java.time.LocalTime startTime) {
        com.example.investmenttradingservice.enums.OrderDirection direction = switch (directionRaw.toLowerCase()) {
            case "buy" -> com.example.investmenttradingservice.enums.OrderDirection.ORDER_DIRECTION_BUY;
            case "sell" -> com.example.investmenttradingservice.enums.OrderDirection.ORDER_DIRECTION_SELL;
            default -> com.example.investmenttradingservice.enums.OrderDirection.ORDER_DIRECTION_BUY;
        };

        List<OrderDTO> orders = new ArrayList<>();

        java.math.BigDecimal priceForLevel = amount.divide(new java.math.BigDecimal(levels.getLevelsCount()), 6,
                java.math.RoundingMode.HALF_UP);

        for (int level = 1; level <= levels.getLevelsCount(); level++) {
            java.math.BigDecimal levelPrice = levels.getLevelValue(level);
            if (levelPrice == null) {
                continue;
            }

            java.math.BigDecimal normalizedLevelPrice = levelPrice.setScale(6, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal adjustedPrice = applyLimitsToPrice(normalizedLevelPrice, direction, instrumentId)
                    .setScale(6, java.math.RoundingMode.HALF_UP);

            int lotSize = calculateLotSize(priceForLevel, adjustedPrice, instrumentId);
            if (lotSize > 0) {
                orders.add(OrderDTO.create(lotSize, adjustedPrice, direction, accountId, instrumentId, startTime));
            }
        }
        return orders;
    }

    // Удалено: generateOrdersForInstrumentWithDirectPrices - больше не используется

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
            if (instrumentServiceFacade == null) {
                logger.warn("InstrumentServiceFacade не доступен, пропускаем генерацию заявок для инструмента: {}",
                        instrumentId);
                return orders;
            }

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

            logger.info("Цена инструмента {}: {}", instrumentId, instrumentPrice);

            // Рассчитываем базовую цену за уровень с округлением до 6 знаков
            BigDecimal priceForLevel = request.amount().divide(
                    new BigDecimal(request.levels().getLevelsCount()),
                    6,
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

            // Рассчитываем цену с учетом процента, направления и лимитов
            BigDecimal adjustedPrice = calculatePriceWithDirection(instrumentPrice, levelPercentage, direction,
                    instrumentId);

            logger.info("Расчет для уровня {}: instrumentPrice={}, levelPercentage={}, direction={}, adjustedPrice={}",
                    level, instrumentPrice, levelPercentage, direction, adjustedPrice);

            // Рассчитываем лотность
            int lotSize = calculateLotSize(basePriceForLevel, adjustedPrice, instrumentId);

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
     * Рассчитывает цену с учетом процента, направления торговли и лимитов
     * инструмента.
     * 
     * @param instrumentPrice цена инструмента
     * @param percentage      процент уровня
     * @param direction       направление торговли
     * @param instrumentId    идентификатор инструмента для получения лимитов
     * @return скорректированная цена с учетом лимитов
     */
    private BigDecimal calculatePriceWithDirection(BigDecimal instrumentPrice, BigDecimal percentage,
            OrderDirection direction, String instrumentId) {
        logger.debug(
                "calculatePriceWithDirection вызван: instrumentPrice={}, percentage={}, direction={}, instrumentId={}",
                instrumentPrice, percentage, direction, instrumentId);

        // Преобразуем процент в десятичную дробь с округлением до 6 знаков
        BigDecimal decimalPercentage = percentage.divide(new BigDecimal("100"), 6, java.math.RoundingMode.HALF_UP);

        // Рассчитываем изменение цены от instrumentPrice
        BigDecimal priceChange = instrumentPrice.multiply(decimalPercentage).setScale(6,
                java.math.RoundingMode.HALF_UP);

        // Рассчитываем базовую цену в зависимости от направления
        BigDecimal basePrice = switch (direction) {
            case ORDER_DIRECTION_SELL -> {
                // Для продажи добавляем процент (цена выше)
                yield instrumentPrice.add(priceChange);
            }
            case ORDER_DIRECTION_BUY -> {
                // Для покупки вычитаем процент (цена ниже)
                yield instrumentPrice.subtract(priceChange);
            }
            default -> {
                logger.warn("Неподдерживаемое направление торговли: {}", direction);
                yield instrumentPrice;
            }
        };

        // Получаем лимиты для инструмента
        BigDecimal adjustedPrice = applyLimitsToPrice(basePrice, direction, instrumentId).setScale(6,
                java.math.RoundingMode.HALF_UP);

        // Округляем до 6 знаков после запятой
        return adjustedPrice.setScale(6, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Применяет лимиты инструмента к рассчитанной цене.
     * 
     * @param price        рассчитанная цена
     * @param direction    направление торговли
     * @param instrumentId идентификатор инструмента
     * @return цена с учетом лимитов
     */
    private BigDecimal applyLimitsToPrice(BigDecimal price, OrderDirection direction, String instrumentId) {
        logger.debug("applyLimitsToPrice вызван для инструмента {}, цена={}, направление={}", instrumentId, price,
                direction);

        try {
            if (tInvestApiService == null) {
                logger.warn("TInvestApiService недоступен, лимиты не учитываются для инструмента: {}", instrumentId);
                return price;
            }

            // Получаем лимиты для инструмента
            List<BigDecimal> limits = tInvestApiService.getLimitsForInstrument(instrumentId);

            logger.debug("Получены лимиты для инструмента {}: {}", instrumentId, limits);

            if (limits == null || limits.isEmpty() || limits.size() < 2) {
                logger.warn("Лимиты не найдены для инструмента: {} (получен список: {}), используем рассчитанную цену",
                        instrumentId, limits);
                return price;
            }

            BigDecimal limitDown = limits.get(0);
            BigDecimal limitUp = limits.get(1);

            logger.debug("Лимиты для инструмента {}: limitDown={}, limitUp={}, текущая цена={}, направление={}",
                    instrumentId, limitDown, limitUp, price, direction);

            // Применяем лимиты: цена должна быть в диапазоне [limitDown, limitUp]
            BigDecimal adjustedPrice = price;

            // Проверяем нижний лимит: цена не должна быть ниже limitDown
            if (limitDown.compareTo(BigDecimal.ZERO) > 0 && price.compareTo(limitDown) < 0) {
                logger.info("Цена {} ограничена снизу лимитом limitDown={} для инструмента {}",
                        price, limitDown, instrumentId);
                adjustedPrice = limitDown;
            }

            // Проверяем верхний лимит: цена не должна быть выше limitUp
            if (limitUp.compareTo(BigDecimal.ZERO) > 0 && adjustedPrice.compareTo(limitUp) > 0) {
                logger.info("Цена {} ограничена сверху лимитом limitUp={} для инструмента {}",
                        adjustedPrice, limitUp, instrumentId);
                adjustedPrice = limitUp;
            }

            // Применяем округление до шага цены
            BigDecimal minPriceIncrement = instrumentServiceFacade != null
                    ? instrumentServiceFacade.getMinPriceIncrement(instrumentId)
                    : null;
            if (minPriceIncrement != null) {
                BigDecimal roundedPrice = calculateAjustedPriceWithMinPriceIncrement(adjustedPrice, minPriceIncrement);

                logger.info("Цена округлена до шага: {} -> {} (шаг: {}, инструмент: {})",
                        adjustedPrice, roundedPrice, minPriceIncrement, instrumentId);

                adjustedPrice = roundedPrice;
            } else {
                logger.warn("Минимальный шаг цены не найден для инструмента: {}, используем исходную цену",
                        instrumentId);
            }

            return adjustedPrice;

        } catch (Exception e) {
            logger.error("Ошибка при применении лимитов для инструмента {}: {}", instrumentId, e.getMessage(), e);
            return price;
        }
    }

    /**
     * Рассчитывает лотность заявки с учетом размера лота инструмента.
     * 
     * @param priceForLevel базовая цена за уровень
     * @param adjustedPrice скорректированная цена
     * @param instrumentId  идентификатор инструмента для получения размера лота
     * @return лотность (округленная в меньшую сторону с учетом размера лота)
     */
    private int calculateLotSize(BigDecimal priceForLevel, BigDecimal adjustedPrice, String instrumentId) {
        if (adjustedPrice.compareTo(BigDecimal.ZERO) == 0) {
            logger.warn("Скорректированная цена равна нулю, невозможно рассчитать лотность");
            return 0;
        }

        // Рассчитываем базовую лотность: priceForLevel / adjustedPrice
        BigDecimal lotRatio = priceForLevel.divide(adjustedPrice, 10, java.math.RoundingMode.HALF_UP);
        int baseLotSize = lotRatio.setScale(0, java.math.RoundingMode.FLOOR).intValue();

        // Получаем размер лота инструмента
        Integer instrumentLot = getLotForInstrument(instrumentId);
        if (instrumentLot == null || instrumentLot <= 0) {
            logger.warn("Размер лота не найден для инструмента: {}, используем базовую лотность: {}",
                    instrumentId, baseLotSize);
            return Math.max(0, baseLotSize);
        }

        // Рассчитываем количество лотов инструмента: baseLotSize / instrumentLot
        int finalLotSize = baseLotSize / instrumentLot;

        logger.info("Расчет лотности: baseLotSize={}, instrumentLot={}, finalLotSize={}, инструмент={}",
                baseLotSize, instrumentLot, finalLotSize, instrumentId);

        return Math.max(0, finalLotSize);
    }

    /**
     * Генерирует список заявок и возвращает цену инструмента.
     * 
     * @param request групповой запрос на создание заявок
     * @return пара: список заявок и цена инструмента
     */
    public java.util.AbstractMap.SimpleEntry<List<OrderDTO>, BigDecimal> generateOrdersWithInstrumentPrice(
            GroupOrderRequest request) {
        logger.info("Начало генерации заявок с ценой инструмента для {} инструментов", request.instruments().size());

        List<OrderDTO> orders = new ArrayList<>();
        BigDecimal instrumentPrice = BigDecimal.ZERO;

        try {
            // Обрабатываем каждый инструмент
            for (String instrumentId : request.instruments()) {
                java.util.AbstractMap.SimpleEntry<List<OrderDTO>, BigDecimal> instrumentResult = generateOrdersForInstrumentWithPrice(
                        instrumentId, request);
                orders.addAll(instrumentResult.getKey());

                // Берем цену инструмента из первого инструмента
                if (instrumentPrice.compareTo(BigDecimal.ZERO) == 0) {
                    instrumentPrice = instrumentResult.getValue();
                }

                logger.debug("Сгенерировано {} заявок для инструмента {} с ценой {}",
                        instrumentResult.getKey().size(), instrumentId, instrumentResult.getValue());
            }

            logger.info("Всего сгенерировано {} заявок с ценой инструмента: {}", orders.size(), instrumentPrice);
            return new java.util.AbstractMap.SimpleEntry<>(orders, instrumentPrice);

        } catch (Exception e) {
            logger.error("Ошибка при генерации заявок с ценой инструмента: {}", e.getMessage(), e);
            return new java.util.AbstractMap.SimpleEntry<>(new ArrayList<>(), BigDecimal.ZERO);
        }
    }

    /**
     * Генерирует заявки для конкретного инструмента и возвращает цену инструмента.
     * 
     * @param instrumentId идентификатор инструмента
     * @param request      групповой запрос
     * @return пара: список заявок для инструмента и цена инструмента
     */
    private java.util.AbstractMap.SimpleEntry<List<OrderDTO>, BigDecimal> generateOrdersForInstrumentWithPrice(
            String instrumentId, GroupOrderRequest request) {
        List<OrderDTO> orders = new ArrayList<>();
        BigDecimal instrumentPrice = BigDecimal.ZERO;

        try {
            if (instrumentServiceFacade == null) {
                logger.warn("InstrumentServiceFacade не доступен, пропускаем генерацию заявок для инструмента: {}",
                        instrumentId);
                return new java.util.AbstractMap.SimpleEntry<>(orders, instrumentPrice);
            }

            // Получаем данные о ценах для инструмента
            List<Object> priceData = instrumentServiceFacade.getPriceDataByType(
                    instrumentId, request.main_price().toLowerCase());

            if (priceData == null || priceData.isEmpty()) {
                logger.warn("Данные о ценах не найдены для инструмента: {}", instrumentId);
                return new java.util.AbstractMap.SimpleEntry<>(orders, instrumentPrice);
            }

            // Получаем цену инструмента
            instrumentPrice = instrumentServiceFacade.extractPriceFromData(priceData.get(0));
            if (instrumentPrice == null) {
                logger.warn("Не удалось извлечь цену инструмента: {}", instrumentId);
                return new java.util.AbstractMap.SimpleEntry<>(orders, BigDecimal.ZERO);
            }

            // Рассчитываем базовую цену за уровень с округлением до 6 знаков
            BigDecimal priceForLevel = request.amount().divide(
                    new BigDecimal(request.levels().getLevelsCount()),
                    6,
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

        return new java.util.AbstractMap.SimpleEntry<>(orders, instrumentPrice);
    }

    private BigDecimal calculateAjustedPriceWithMinPriceIncrement(BigDecimal price, BigDecimal minPriceIncrement) {
        return price.divide(minPriceIncrement, 6, java.math.RoundingMode.DOWN)
                .multiply(minPriceIncrement)
                .setScale(6, java.math.RoundingMode.DOWN);
    }

    /**
     * Получает размер лота для инструмента.
     * 
     * @param instrumentId идентификатор инструмента
     * @return размер лота или null если не найден
     */
    private Integer getLotForInstrument(String instrumentId) {
        try {
            if (instrumentServiceFacade == null) {
                logger.warn("InstrumentServiceFacade недоступен для получения размера лота инструмента: {}",
                        instrumentId);
                return null;
            }

            return instrumentServiceFacade.getLot(instrumentId);
        } catch (Exception e) {
            logger.error("Ошибка при получении размера лота для инструмента {}: {}",
                    instrumentId, e.getMessage(), e);
            return null;
        }
    }
}
