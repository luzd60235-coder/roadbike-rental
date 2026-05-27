package com.example.roadbikerental.common.enums;

/**
 * 维修状态枚举。
 */
public enum MaintenanceStatusEnum {

    PENDING(1),
    PROCESSING(2),
    COMPLETED(3);

    private final int code;

    MaintenanceStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
