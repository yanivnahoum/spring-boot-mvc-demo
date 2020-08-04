package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.tc.SpringBootMySqlSingletonTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootMySqlSingletonTest
@AutoConfigureMockMvc
class AndStillAnotherUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Test
    void whenGetUsers_shouldReturn200OK_withAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int) userRepository.count())));
    }

    @Test
    void givenId1_whenDeleteUser_shouldReturn200Ok() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}

@SpringBootMySqlSingletonTest
@AutoConfigureMockMvc
@DisplayName("When calling GET /users/{id}")
class GetSingleUser {

    @Autowired
    private MockMvc mockMvc;

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
}