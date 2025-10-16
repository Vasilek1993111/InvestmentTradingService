package com.example.investmenttradingservice.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.example.investmenttradingservice.DTO.OrderDTO;
import com.example.investmenttradingservice.entity.OrderEntity;
import com.example.investmenttradingservice.mapper.OrderMapper;
import com.example.investmenttradingservice.enums.OrderStatus;

/**
 * OrderCacheService — высокопроизводительный локальный кэш заявок.
 *
 * <p>
 * Хранит заявки до момента отправки в T-Invest API. Источником истины служит
 * БД,
 * но планировщик читает из кэша для минимальной задержки.
 * </p>
 */
@Service
public class OrderCacheService {

    private static final Logger logger = LoggerFactory.getLogger(OrderCacheService.class);

    private final ConcurrentMap<String, OrderEntity> orderById = new ConcurrentHashMap<>();
    private final ConcurrentSkipListMap<LocalTime, Set<String>> idsByTime = new ConcurrentSkipListMap<>();

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderPersistenceService orderPersistenceService;

    /**
     * Кладет заявку в кэш (OrderDTO конвертируется в OrderEntity).
     *
     * @param orderDTO заявка DTO
     */
    public void put(OrderDTO orderDTO) {
        if (orderDTO == null || !orderDTO.isValid()) {
            return;
        }
        OrderEntity entity = orderMapper.toEntity(orderDTO);
        // Нормализуем время до секунд (обнуляем наносекунды) для согласованности с
        // планировщиком
        if (entity.getScheduledTime() != null) {
            entity.setScheduledTime(entity.getScheduledTime().withSecond(0).withNano(0));
        }
        orderById.put(entity.getOrderId(), entity);
        idsByTime.compute(entity.getScheduledTime(), (time, set) -> {
            if (set == null)
                set = Collections.synchronizedSet(new HashSet<>());
            set.add(entity.getOrderId());
            return set;
        });
        logger.debug("Кэш: добавлена заявка {}, время {}", entity.getOrderId(), entity.getScheduledTime());
    }

    /**
     * Кладет список заявок в кэш.
     */
    public void putAll(List<OrderDTO> orders) {
        if (orders == null || orders.isEmpty())
            return;
        for (OrderDTO dto : orders) {
            put(dto);
        }
        logger.info("Кэш: добавлено заявок: {}", orders.size());
    }

    /**
     * Возвращает заявки с точным временем (не просроченные).
     * 
     * @param exactTime точное время для поиска заявок
     * @return список заявок с указанным временем
     */
    public List<OrderEntity> getExactTimeOrders(LocalTime exactTime) {
        if (idsByTime.isEmpty()) {
            return List.of();
        }

        List<OrderEntity> exactOrders = new ArrayList<>();
        Set<String> idSet = idsByTime.get(exactTime);

        if (idSet != null) {
            synchronized (idSet) {
                for (String id : idSet) {
                    OrderEntity e = orderById.get(id);
                    if (e != null) {
                        exactOrders.add(e);
                    }
                }
            }
        }

        return exactOrders;
    }

    /**
     * Удаляет заявку из кэша по её ID.
     */
    public void remove(String orderId) {
        if (orderId == null)
            return;
        OrderEntity removed = orderById.remove(orderId);
        if (removed != null) {
            LocalTime time = removed.getScheduledTime();
            idsByTime.computeIfPresent(time, (t, set) -> {
                synchronized (set) {
                    set.remove(orderId);
                    return set.isEmpty() ? null : set;
                }
            });
            logger.debug("Кэш: удалена заявка {}", orderId);
        }
    }

    /**
     * Прогревает кэш из БД заявками в статусе PENDING.
     */
    public void warmupPending() {
        try {
            List<OrderDTO> pending = orderPersistenceService.findOrdersByStatusDTO(OrderStatus.PENDING);
            if (pending != null && !pending.isEmpty()) {
                putAll(pending);
                logger.info("Кэш заявок прогрет из БД: загружено PENDING: {}", pending.size());
            } else {
                logger.info("Кэш заявок: нет PENDING заявок для прогрева");
            }
        } catch (Exception e) {
            logger.error("Ошибка прогрева кэша заявок: {}", e.getMessage(), e);
        }
    }

    /**
     * Автоматический прогрев кэша заявок при старте приложения (PENDING).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupOnStartup() {
        warmupPending();
    }

    /**
     * Возвращает все заявки из кэша как Entity.
     */
    public List<OrderEntity> getAllEntities() {
        return new ArrayList<>(orderById.values());
    }

    /**
     * Возвращает все заявки из кэша как DTO.
     */
    public List<OrderDTO> getAllDTOs() {
        List<OrderDTO> result = new ArrayList<>();
        for (OrderEntity e : orderById.values()) {
            result.add(orderMapper.toDTO(e));
        }
        return result;
    }
}
