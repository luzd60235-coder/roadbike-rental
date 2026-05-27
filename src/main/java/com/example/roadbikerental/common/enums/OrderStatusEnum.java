package com.example.roadbikerental.common.enums;

/**
 * 订单状态枚举。
 */
public enum OrderStatusEnum {

    PENDING_PAYMENT(1),
    PENDING_PICKUP(2),
    RENTING(3),
    PENDING_SETTLEMENT(4),
    COMPLETED(5),
    CANCELED(6);

    private final int code;

    OrderStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
