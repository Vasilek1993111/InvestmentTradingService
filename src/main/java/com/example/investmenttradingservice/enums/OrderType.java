package com.example.investmenttradingservice.enums;

/**
 * Enum для типа заявки в T-Invest API.
 */
public enum OrderType {
    ORDER_TYPE_UNSPECIFIED("ORDER_TYPE_UNSPECIFIED"),
    ORDER_TYPE_LIMIT("ORDER_TYPE_LIMIT"),
    ORDER_TYPE_MARKET("ORDER_TYPE_MARKET");
    
    private final String value;
    
    OrderType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
