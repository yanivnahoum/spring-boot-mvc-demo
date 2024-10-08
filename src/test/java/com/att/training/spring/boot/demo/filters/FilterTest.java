package com.att.training.spring.boot.demo.filters;

import io.restassured.RestAssured;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilterTest {
    @Nested
    class StandaloneMockMvcFilterTest {
        private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HelloController())
                .addFilter(new GreetingFilter())
                .build();

        @Test
        void greetingFilter_shouldAddGreetingInRequestAttribute_withStandaloneMockMvc() throws Exception {
            mockMvc.perform(get(HelloController.GREET_PATH))
                    .andExpect(status().isOk())
                    .andExpect(content().string(GreetingFilter.GREETING_VALUE));
        }
    }

    @Nested
    @WebMvcTest(HelloController.class)
    class WebMvcFilterTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void greetingFilter_shouldAddGreetingInRequestAttribute() throws Exception {
            mockMvc.perform(get(HelloController.GREET_PATH))
                    .andExpect(status().isOk())
                    .andExpect(content().string(GreetingFilter.GREETING_VALUE));
        }
    }


    @Nested
    @SpringBootTest(classes = {HelloController.class, GreetingFilter.class})
    @AutoConfigureMockMvc
    class SpringBootFilterTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void greetingFilter_shouldAddGreetingInRequestAttribute() throws Exception {
            mockMvc.perform(get(HelloController.GREET_PATH))
                    .andExpect(status().isOk())
                    .andExpect(content().string(GreetingFilter.GREETING_VALUE));
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = RANDOM_PORT)
    class SpringBootServerFilterTest {
        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void greetingFilter_shouldAddGreetingInRequestAttribute() {
            ResponseEntity<String> response = restTemplate.getForEntity(HelloController.GREET_PATH, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(GreetingFilter.GREETING_VALUE);
        }
    }

    @Disabled("""
            Throws an exception when running tests with maven only:
            MissingMethod No signature of method: static io.restassured.internal.http.URIBuilder.encode() is applicable for argument types: (String, String)
            """)
    @Nested
    @SpringBootTest(webEnvironment = RANDOM_PORT)
    @TestInstance(PER_CLASS)
    class SpringBootServerRestAssuredFilterTest {
        @LocalServerPort
        private int port;
        @Value("${server.servlet.context-path}")
        private String contextPath;

        @BeforeAll
        void init() {
            RestAssured.basePath = contextPath;
            RestAssured.port = port;
        }

        @Test
        void greetingFilter_shouldAddGreetingInRequestAttribute() {
            given()
                    .when().get(HelloController.GREET_PATH)
                    .then().assertThat().statusCode(equalTo(200))
                    .and().assertThat().body(equalTo(GreetingFilter.GREETING_VALUE));
        }
    }

    @RestController
    static class HelloController {
        static final String GREET_PATH = "/greet";

        @GetMapping(GREET_PATH)
        public String greet(@RequestAttribute String greeting) {
            return greeting;
        }
    }

    @Component
    static class GreetingFilter extends OncePerRequestFilter {
        private static final String GREETING_KEY = "greeting";
        static final String GREETING_VALUE = "Hello!";

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain) throws IOException, ServletException {
            request.setAttribute(GREETING_KEY, GREETING_VALUE);
            filterChain.doFilter(request, response);
        }
    }
}
