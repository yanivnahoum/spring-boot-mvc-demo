package com.att.training.spring.boot.demo.errors;

import com.att.training.spring.boot.demo.api.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlers extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ErrorDto handleUserNotFound(UserNotFoundException ex) {
        log.error("#handleUserNotFound - ", ex);
        String message = "User not found: " + ex.getMessage();
        return new ErrorDto(ErrorCode.NOT_FOUND, message);
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorDto handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("#handleConstraintViolationException - ", ex);
        String message = buildMessage(ex);
        return new ErrorDto(ErrorCode.VALIDATION, message);
    }

    @ExceptionHandler
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorDto handleGenericException(Exception ex) {
        log.error("#handleGenericException - ", ex);
        return new ErrorDto(ErrorCode.GENERIC, ex.getMessage());
    }

    private String buildMessage(ConstraintViolationException ex) {
        return ex.getConstraintViolations()
                 .stream()
                 .map(this::toMessage)
                 .collect(joining(", "));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        log.error("#handleMethodArgumentNotValid - ", ex);
        String message = buildMessage(ex);
        ErrorDto errorDto = new ErrorDto(ErrorCode.VALIDATION, message);
        return new ResponseEntity<>(errorDto, status);
    }

    private String buildMessage(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        return bindingResult.getFieldErrors()
                            .stream()
                            .map(this::toMessage)
                            .collect(joining(", "));
    }

    private String toMessage(ConstraintViolation<?> constraintViolation) {
        return String.format("Field '%s' %s", constraintViolation.getPropertyPath(), constraintViolation.getMessage());
    }

    private String toMessage(FieldError error) {
        return String.format("Field '%s.%s' %s", error.getObjectName(), error.getField(), error.getDefaultMessage());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        log.error("#handleExceptionInternal - ", ex);
        ErrorDto errorDto = new ErrorDto(ErrorCode.GENERIC, ex.getMessage());
        return new ResponseEntity<>(errorDto, status);
    }
}
