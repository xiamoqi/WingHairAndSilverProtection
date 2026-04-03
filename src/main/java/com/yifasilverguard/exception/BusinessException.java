package com.yifasilverguard.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final String code;    // 错误码
    private final String message; // 错误消息

    // 只传错误消息
    public BusinessException(String message) {
        super(message);
        this.code = null;
        this.message = message;
    }
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    // 传错误消息和原始异常
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
        this.message = message;
    }
}
