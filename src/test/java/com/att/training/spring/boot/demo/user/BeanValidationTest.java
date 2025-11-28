package com.att.training.spring.boot.demo.user;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(UserController.class)
@MockitoBean(types = UserService.class)
@Slf4j
class BeanValidationTest {
    @Autowired
    private MockMvcTester mockMvc;

    // This will throw a ConstraintValidationException if we annotate the controller with @Validated.
    // See: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html
    @Test
    void directValidationOnMethodParameterThrowsHandlerMethodValidationException() throws UnsupportedEncodingException {
        var result = mockMvc.get()
                .uri("/users/{id}", 0)
                .exchange();

        assertThat(result).hasStatus(BAD_REQUEST);
        log.info(result.getResponse().getContentAsString());
    }

    @Test
    void recursiveValidationOnMethodParameterThrowsMethodArgumentNotValidException() throws UnsupportedEncodingException {
        var result = mockMvc.put()
                .uri("/users")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                            "id": -1,
                            "firstName": "",
                            "lastName": "Jordan",
                            "age": 50
                        }
                        """).exchange();


        assertThat(result).hasStatus(BAD_REQUEST);
        log.info(result.getResponse().getContentAsString());
    }
}
