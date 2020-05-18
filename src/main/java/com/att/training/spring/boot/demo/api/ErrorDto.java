package com.att.training.spring.boot.demo.api;

import com.att.training.spring.boot.demo.errors.ErrorCode;
import lombok.Value;

@Value
public class ErrorDto {
    ErrorCode code;
    String message;
}
