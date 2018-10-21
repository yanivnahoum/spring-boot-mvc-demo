package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.errors.ExceptionHandlers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserConfiguration userConfiguration;

    @Nested
    @DisplayName("When calling GET /users/{id}")
    class GetSingleUser {

        @Test
        void givenId1_shouldReturn200_withMichaelAsJson() throws Exception {
            mockMvc.perform(get("/users/{id}", 1))
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(content().json("{'id':1,'firstName':'Michael','lastName':'Jordan','age':50}"))
                   .andExpect(jsonPath("$.firstName", is("Michael")));
        }

        @Test
        void givenId1_shouldReturnMichaelAsJson() throws Exception {
            String json = mockMvc.perform(get("/users/{id}", 1))
                                 .andDo(print())
                                 .andExpect(status().isOk())
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();

            JSONAssert.assertEquals(json, "{'id':1,'firstName':'Michael','lastName':'Jordan','age':50}", LENIENT);
        }

        @Test
        void givenMissingId_shouldReturn404NotFound() throws Exception {
            int id = 4;
            String expectedJson = String.format("{ code: 5001, message = 'User not found: %d' }", id);
            mockMvc.perform(get("/users/{id}", id))
                   .andExpect(status().isNotFound())
                   .andExpect(content().json(expectedJson));
        }

        @Test
        void whenUserServiceThrowsGenericException_shouldReturn500InternalServerError() throws Exception {
            UserService userService = mock(UserService.class);
            mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
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

    @Nested
    @DisplayName("When calling GET /users")
    class GetAllUsers {

        @Test
        void shouldReturn200_withAllUsers() throws Exception {
            mockMvc.perform(get("/users"))
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$", hasSize(userConfiguration.getUsers().size())));
        }
    }
}
