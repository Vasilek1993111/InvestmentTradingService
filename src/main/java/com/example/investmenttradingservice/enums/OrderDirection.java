package com.example.investmenttradingservice.enums;

/**
 * Enum для направления заявки в T-Invest API.
 */
public enum OrderDirection {
    ORDER_DIRECTION_UNSPECIFIED("ORDER_DIRECTION_UNSPECIFIED"),
    ORDER_DIRECTION_BUY("ORDER_DIRECTION_BUY"),
    ORDER_DIRECTION_SELL("ORDER_DIRECTION_SELL");
    
    private final String value;
    
    OrderDirection(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Преобразует строковое направление в enum.
     * 
     * @param direction строковое направление (buy/sell)
     * @return OrderDirection enum
     */
    public static OrderDirection fromString(String direction) {
        return switch (direction.toLowerCase()) {
            case "buy" -> ORDER_DIRECTION_BUY;
            case "sell" -> ORDER_DIRECTION_SELL;
            default -> ORDER_DIRECTION_UNSPECIFIED;
        };
    }
}
