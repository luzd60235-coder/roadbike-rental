package com.example.roadbikerental.common.exception;

/**
 * 业务异常。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
