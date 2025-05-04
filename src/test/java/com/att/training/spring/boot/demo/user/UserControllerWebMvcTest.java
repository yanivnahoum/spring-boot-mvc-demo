package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(UserController.class)
class UserControllerWebMvcTest {
    private static final User JOHN_DOE = new User(17, "John", "Doe", 30);
    private static final List<User> USERS = List.of(JOHN_DOE, JOHN_DOE, JOHN_DOE);
    private static final List<User> SINGLE_USER = List.of(JOHN_DOE);

    @MockitoBean
    private UserService userService;
    @Autowired
    private MockMvcTester mockMvc;

    @Nested
    @DisplayName("When calling GET /users")
    class GetAllUsers {
        @Test
        void givenServiceReturnsMultipleUsers_shouldReturn200OK_withMultipleUsers() {
            when(userService.fetchAll()).thenReturn(USERS);

            var result = mockMvc.get().uri("/users");

            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .extractingPath("$.length()").isEqualTo(USERS.size());

        }

        @Test
        void givenServiceReturnsSingleUser_shouldReturn200OK_withSingleUser() {
            when(userService.fetchAll()).thenReturn(SINGLE_USER);

            var result = mockMvc.get().uri("/users");

            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .extractingPath("$.length()").isEqualTo(SINGLE_USER.size());
        }

        @Test
        void givenServiceReturnsEmptyList_shouldReturn200OK_withNoUsers() {
            var result = mockMvc.get().uri("/users");

            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .extractingPath("$.length()").isEqualTo(0);
        }
    }

    @Test
    void givenUserInBody_whenPUT_shouldReturn200OK() {
        var result = mockMvc.put()
                .uri("/users")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                            "id": 1,
                            "firstName": "Michael",
                            "lastName": "Jordan",
                            "age": 50
                        }
                        """);

        assertThat(result).hasStatus(NO_CONTENT);
    }
}
