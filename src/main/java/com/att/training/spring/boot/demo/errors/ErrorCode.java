package com.att.training.spring.boot.demo.errors;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCode {

    NOT_FOUND(5001),
    VALIDATION(5002),
    GENERIC(9999);

    @Getter
    @JsonValue
    private final int code;
}