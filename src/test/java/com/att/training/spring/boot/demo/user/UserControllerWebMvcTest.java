package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerWebMvcTest {

    private static final User JOHN_DOE = new User(17, "John", "Doe", 30);
    private static final List<User> USERS = List.of(JOHN_DOE, JOHN_DOE, JOHN_DOE);
    private static final List<User> SINGLE_USER = List.of(JOHN_DOE);

    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("When calling GET /users")
    class GetAllUsers {

        @Test
        void givenServiceReturnsMultipleUsers_shouldReturn200OK_withMultipleUsers() throws Exception {
            when(userService.fetchAll()).thenReturn(USERS);
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(USERS.size()))));
        }

        @Test
        void givenServiceReturnsSingleUser_shouldReturn200OK_withSingleUser() throws Exception {
            when(userService.fetchAll()).thenReturn(SINGLE_USER);
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(SINGLE_USER.size()))));
        }

        @Test
        void givenServiceReturnsEmptyList_shouldReturn200OK_withNoUsers() throws Exception {
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(equalTo(0))));
        }

    }

    @Test
    void givenUserInBody_whenPUT_shouldReturn200OK() throws Exception {
        mockMvc.perform(put("/users")
                .contentType(APPLICATION_JSON)
                .content("{\"id\":1,\"firstName\":\"Michael\",\"lastName\":\"Jordan\",\"age\":50}"))
                .andExpect(status().isNoContent());
    }
}
