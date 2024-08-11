package com.att.training.spring.boot.demo.api;

import com.google.common.base.MoreObjects;

public class ErrorInfo {

    private final int code;
    private final String message;

    public ErrorInfo(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("message", message)
                .toString();
    }
}
