package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.errors.ExceptionHandlers;
import com.att.training.spring.boot.demo.tc.SpringBootMySqlTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootMySqlTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class AnotherUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper mapper;

    @Nested
    @DisplayName("When calling GET /users")
    class GetAllUsers {

        @Test
        void shouldReturn200OK_withAllUsers() throws Exception {
            mockMvc.perform(get("/users"))
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$", hasSize((int) userRepository.count())));
        }
    }

    @Nested
    @DisplayName("When calling GET /users/{id}")
    class GetSingleUser {

        @Test
        void givenId1_shouldReturn200OK_withMichaelAsJson() throws Exception {
            mockMvc.perform(get("/users/{id}", 1))
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(content().json("{'id':1,'firstName':'Michael','lastName':'Jordan','age':50}"))
                   .andExpect(jsonPath("$.firstName", is("Michael")));
        }

        @Test
        void givenMissingId_shouldReturn404NotFound() throws Exception {
            int id = 4;
            String expectedJson = String.format("{ code: 5001, message = 'UserDto not found: %d' }", id);
            mockMvc.perform(get("/users/{id}", id))
                   .andDo(print())
                   .andExpect(status().isNotFound())
                   .andExpect(content().json(expectedJson));
        }

        @Test
        void whenUserServiceThrowsGenericException_shouldReturn500InternalServerError(@Mock UserService userService) throws Exception {
            mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, mapper))
                                     .setControllerAdvice(new ExceptionHandlers())
                                     .build();

            String errorMessage = "Thrown intentionally by mock!";
            int id = 2;
            when(userService.fetch(id)).thenThrow(new IllegalArgumentException(errorMessage));
            String expectedJson = String.format("{ code: 9999, message = '%s' }", errorMessage);

            mockMvc.perform(get("/users/{id}", id))
                   .andDo(print())
                   .andExpect(status().isInternalServerError())
                   .andExpect(content().json(expectedJson));
        }
    }

//    @DirtiesContext(methodMode = AFTER_METHOD)
//    @Disabled("to prevent reloading the context")
    @Test
    void whenDeleteUser_givenId1_shouldReturn200Ok() throws Exception {
        mockMvc.perform(delete("/users/1"))
               .andDo(print())
               .andExpect(status().isOk());
    }
}

