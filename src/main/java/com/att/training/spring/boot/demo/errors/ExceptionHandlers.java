package com.att.training.spring.boot.demo.errors;

import com.att.training.spring.boot.demo.api.ErrorDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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
        log.error("#handleUserNotFound", ex);
        String message = "User not found: " + ex.getMessage();
        return new ErrorDto(ErrorCode.NOT_FOUND, message);
    }

    // This is no longer needed. It is thrown only in case the Controller is annotated with @Validated,
    // and the bean validation annotation is directly on a method parameter (i.e. @NotNull, but not @Valid)
    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorDto handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("#handleConstraintViolationException", ex);
        String message = buildMessage(ex);
        return new ErrorDto(ErrorCode.VALIDATION, message);
    }

    private String buildMessage(ConstraintViolationException ex) {
        return ex.getConstraintViolations()
                .stream()
                .map(this::toMessage)
                .collect(joining(", "));
    }

    private String toMessage(ConstraintViolation<?> constraintViolation) {
        return "Field '%s' %s".formatted(constraintViolation.getPropertyPath(), constraintViolation.getMessage());
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                            HttpStatusCode status, WebRequest request) {
        log.error("#handleMethodArgumentNotValid", ex);
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

    private String toMessage(FieldError error) {
        return "Field '%s.%s' %s".formatted(error.getObjectName(), error.getField(), error.getDefaultMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers,
                                                                            HttpStatusCode status, WebRequest request) {
        log.error("#handleHandlerMethodValidationException", ex);
        String message = buildMessage(ex);
        ErrorDto errorDto = new ErrorDto(ErrorCode.VALIDATION, message);
        return new ResponseEntity<>(errorDto, status);
    }

    private String buildMessage(HandlerMethodValidationException ex) {
        var allErrors = ex.getBeanResults();
        return allErrors.stream()
                .flatMap(error -> error.getFieldErrors().stream())
                .map(this::toMessage)
                .collect(joining(", "));
    }

    @ExceptionHandler
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorDto handleGenericException(Exception ex) {
        log.error("#handleGenericException", ex);
        return new ErrorDto(ErrorCode.GENERIC, ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        log.error("#handleExceptionInternal", ex);
        ErrorDto errorDto = new ErrorDto(ErrorCode.GENERIC, ex.getMessage());
        return new ResponseEntity<>(errorDto, statusCode);
    }
}
