package com.att.training.spring.boot.demo.api;

import com.att.training.spring.boot.demo.errors.ErrorCode;


public record ErrorDto(ErrorCode code, String message) {}
