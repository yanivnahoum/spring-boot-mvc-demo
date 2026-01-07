package com.att.training.spring.boot.demo.errors;

import com.att.training.spring.boot.demo.api.ErrorDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Stream;

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

    // This is the exception thrown when the validation annotations are on the fields of a bean,
    // and the controller method parameter is annotated with @Valid
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status, @NonNull WebRequest request) {
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

    // This is the exception thrown when the validation annotations are on the controller
    // method parameter itself - when it's annotated with a constraint such as @NotEmpty, @Min, etc.
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(@NonNull HandlerMethodValidationException ex, @NonNull HttpHeaders headers,
                                                                            @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        log.error("#handleHandlerMethodValidationException", ex);
        String message = buildMessage(ex);
        ErrorDto errorDto = new ErrorDto(ErrorCode.VALIDATION, message);
        return new ResponseEntity<>(errorDto, status);
    }

    private String buildMessage(HandlerMethodValidationException ex) {
        return ex.getParameterValidationResults().stream()
                .flatMap(this::allParameterValidationErrors)
                .collect(joining());
    }

    private Stream<String> allParameterValidationErrors(ParameterValidationResult result) {
        return result.getResolvableErrors().stream()
                .map(error -> toMessage(result, error));
    }

    private String toMessage(ParameterValidationResult result, MessageSourceResolvable error) {
        if (error instanceof FieldError fieldError) {
            return toMessage(fieldError);
        }
        return "Parameter '%s' %s".formatted(result.getMethodParameter().getParameterName(), error.getDefaultMessage());
    }

    @ExceptionHandler
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorDto handleGenericException(Exception ex) {
        log.error("#handleGenericException", ex);
        return new ErrorDto(ErrorCode.GENERIC, ex.getMessage());
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex, Object body, @NonNull HttpHeaders headers,
                                                             @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {
        log.error("#handleExceptionInternal", ex);
        ErrorDto errorDto = new ErrorDto(ErrorCode.GENERIC, ex.getMessage());
        return new ResponseEntity<>(errorDto, statusCode);
    }
}
