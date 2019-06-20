package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.Slow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slow
@WebMvcTest(UserController.class)
@Import(UserService.class)
class UserControllerWebMvcTest {

    private static final User johnDoe = new User(17L, "John", "Doe", 30);
    private static final List<User> USERS = List.of(johnDoe, johnDoe, johnDoe);
    private static final List<User> SINGLE_USER = List.of(johnDoe);

    @MockBean
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenServiceReturnsMultipleUsers_shouldReturn200OK_withMultipleUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(USERS);
        mockMvc.perform(get("/users"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()", is(equalTo(USERS.size()))));
    }

    @Test
    void givenServiceReturnsSingleUser_shouldReturn200OK_withSingleUser() throws Exception {
        when(userRepository.findAll()).thenReturn(SINGLE_USER);
        mockMvc.perform(get("/users"))
               .andExpect(status().isOk())
               .andExpect(content().json(String.format("[{'id':%d,'firstName':'%s','lastName':'%s','age':%d}]",
                       johnDoe.getId(), johnDoe.getFirstName(), johnDoe.getLastName(), johnDoe.getAge())));
    }

    @Test
    void givenServiceReturnsEmptyList_shouldReturn200OK_withNoUsers() throws Exception {
        mockMvc.perform(get("/users"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()", is(equalTo(0))))
               .andExpect(content().json("[]"));
    }

    // @MockBean doesn't get reset in @Nested classes: https://github.com/spring-projects/spring-boot/issues/12470
    @Nested
    @DisplayName("When calling GET /users")
    class GetAllUsers {
    }
}
