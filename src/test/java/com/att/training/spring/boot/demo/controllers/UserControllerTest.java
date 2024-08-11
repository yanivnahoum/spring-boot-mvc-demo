package com.att.training.spring.boot.demo.controllers;

import com.att.training.spring.boot.demo.core.UserConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserConfiguration userConfiguration;

    @Test
    public void getUsersWithId1_shouldReturn200_withMichaelAsJson() throws Exception {
        mockMvc.perform(get("/users/{id}", 1))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().json("{'id':1,'firstName':'Michael','lastName':'Jordan','age':50}"))
               .andExpect(jsonPath("$.firstName", is("Michael")));
    }

    @Test
    public void getUsersWithId1_shouldReturnMichaelAsJson() throws Exception {
        String json = mockMvc.perform(get("/users/{id}", 1))
                             .andDo(print())
                             .andExpect(status().isOk())
                             .andReturn()
                             .getResponse()
                             .getContentAsString();

        JSONAssert.assertEquals(json, "{'id':1,'firstName':'Michael','lastName':'Jordan','age':50}", false);
    }

    @Test
    public void getUsers_shouldReturn200_withAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(userConfiguration.getUsers().size())));
    }

    @Test
    public void getUsers_withMissingId_shouldReturn404NotFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 4))
               .andExpect(status().isNotFound());
    }
}