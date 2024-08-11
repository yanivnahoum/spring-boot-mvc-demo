package com.att.training.spring.boot.demo.errors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.att.training.spring.boot.demo.api.ErrorInfo;

@RestControllerAdvice
public class ExceptionHandlers {
  
    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ErrorInfo userNotFound(UserNotFoundException e) {
        String message = "User not found: " + e.getMessage();
        return new ErrorInfo(5001, message);
    }

}
