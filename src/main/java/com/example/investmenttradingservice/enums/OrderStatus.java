package com.example.investmenttradingservice.enums;

/**
 * Enum для статуса заявки.
 */
public enum OrderStatus {
    /** Заявка создана и ожидает отправки */
    PENDING("PENDING"),
    
    /** Заявка отправлена в T-Invest API */
    SENT("SENT"),
    
    /** Заявка исполнена */
    EXECUTED("EXECUTED"),
    
    /** Заявка отклонена */
    REJECTED("REJECTED"),
    
    /** Заявка отменена */
    CANCELLED("CANCELLED"),
    
    /** Ошибка при обработке заявки */
    ERROR("ERROR");
    
    private final String value;
    
    OrderStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Преобразует строковый статус в enum.
     * 
     * @param status строковый статус
     * @return OrderStatus enum
     */
    public static OrderStatus fromString(String status) {
        if (status == null) {
            return PENDING;
        }
        
        return switch (status.toUpperCase()) {
            case "PENDING" -> PENDING;
            case "SENT" -> SENT;
            case "EXECUTED" -> EXECUTED;
            case "REJECTED" -> REJECTED;
            case "CANCELLED" -> CANCELLED;
            case "ERROR" -> ERROR;
            default -> PENDING;
        };
    }
}
