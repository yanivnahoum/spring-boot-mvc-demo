package com.att.training.spring.boot.demo.api;

import com.att.training.spring.boot.demo.errors.ErrorCode;
import lombok.Value;


public @Value class ErrorDto {

    int code;
    String message;

    public ErrorDto(ErrorCode errorCode, String message) {
        this.code = errorCode.getCode();
        this.message = message;
    }
}
