package com.example.investmenttradingservice.DTO;

import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

/**
 * DTO для возврата реального ответа PostOrderResponse из T-Invest API.
 */
public record TinkoffPostOrderResponseDTO(
        String orderId,
        String message,
        Long executedUnits,
        Integer executedNano,
        String direction,
        String orderType) {

    public static TinkoffPostOrderResponseDTO from(PostOrderResponse r) {
        Long units = r.hasExecutedOrderPrice() ? r.getExecutedOrderPrice().getUnits() : null;
        Integer nano = r.hasExecutedOrderPrice() ? r.getExecutedOrderPrice().getNano() : null;
        return new TinkoffPostOrderResponseDTO(
                r.getOrderId(),
                r.getMessage(),
                units,
                nano,
                r.getDirection().name(),
                r.getOrderType().name());
    }
}
