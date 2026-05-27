package com.example.roadbikerental.common.enums;

/**
 * 车辆状态枚举。
 */
public enum BikeStatusEnum {

    AVAILABLE(1),
    RESERVED(2),
    RENTING(3),
    MAINTENANCE(4),
    DISABLED(5);

    private final int code;

    BikeStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
