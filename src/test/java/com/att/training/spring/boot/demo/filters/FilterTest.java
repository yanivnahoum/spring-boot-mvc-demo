package com.att.training.spring.boot.demo.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
    @Import(GreetingFilter.class)
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
    @AutoConfigureRestTestClient
    @Import(GreetingFilter.class)
    class SpringBootServerFilterTest {
        @Autowired
        private RestTestClient restClient;

        @Test
        void greetingFilter_shouldAddGreetingInRequestAttribute() {
            restClient.get().uri(HelloController.GREET_PATH).exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class).isEqualTo(GreetingFilter.GREETING_VALUE);
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

    @TestComponent
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
