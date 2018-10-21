package com.att.training.spring.boot.demo.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    NOT_FOUND(5001),
    VALIDATION(5002),
    GENERIC(9999);

    private final int code;
}
