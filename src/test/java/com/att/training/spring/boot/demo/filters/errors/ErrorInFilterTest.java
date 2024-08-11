package com.att.training.spring.boot.demo.filters.errors;

import com.att.training.spring.boot.demo.api.ErrorDto;
import com.att.training.spring.boot.demo.errors.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({
        SpringBootServerThrowingFilterTest.ThrowingFilter.class,
        SpringBootServerThrowingFilterTest.DefaultController.class,
        SpringBootServerThrowingFilterTest.DefaultErrorController.class,
        SpringBootServerThrowingFilterTest.GlobalExceptionHandler.class

})
class SpringBootServerThrowingFilterTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void whenFilterThrows_shouldReachErrorControllerAndThenGlobalExceptionHandler() throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity(DefaultController.REQUEST_PATH, String.class);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        var actualErrorDto = mapper.readValue(response.getBody(), ErrorDto.class);
        var expectedErrorDto = new ErrorDto(ErrorCode.GENERIC, ThrowingFilter.ERROR_MESSAGE);
        assertThat(actualErrorDto).isEqualTo(expectedErrorDto);
    }

    @TestComponent
    @RestController
    static class DefaultController {

        static final String REQUEST_PATH = "/simple";
        static final String REQUEST_RESULT = "Success";

        @GetMapping(REQUEST_PATH)
        public String request() {
            return REQUEST_RESULT;
        }

    }

    @TestComponent
    @Controller
    @Slf4j
    static class DefaultErrorController implements ErrorController {

        @RequestMapping("/error")
        public void error(HttpServletRequest request) throws Throwable {
            log.info("#error - rethrowing the exception...");
            var ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            if (ex != null) {
                throw ex;
            }
        }

    }

    @TestComponent
    @RestControllerAdvice
    @Slf4j
    static class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        @ResponseStatus(BAD_REQUEST)
        @ExceptionHandler
        ErrorDto handleSomeSpecificException(SomeSpecificException ex) {
            log.info("#handleSomeSpecificException - returning error DTO...");
            return new ErrorDto(ErrorCode.GENERIC, ex.getMessage());
        }

        @NonNull
        @Override
        protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex, Object body, @NonNull HttpHeaders headers,
                                                                 @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {
            ErrorDto errorDto = new ErrorDto(ErrorCode.GENERIC, ex.getMessage());
            return new ResponseEntity<>(errorDto, statusCode);
        }

    }

    @TestComponent
    @Slf4j
    static class ThrowingFilter extends OncePerRequestFilter {
        static final String ERROR_MESSAGE = "an error occurred in the filter!";

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain) {
            log.info("#doFilterInternal - throwing from filter...");
            throw new SomeSpecificException(ERROR_MESSAGE);
        }


    }

    static class SomeSpecificException extends RuntimeException {

        SomeSpecificException(String message) {
            super(message);
        }
    }
}
/**
 * This is to limit the scope of the test app
 */
@SpringBootApplication
class SpringApp {
    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }
}
