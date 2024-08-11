package com.att.training.spring.boot.demo;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimpleController.class)
class ControllerMethodParametersTest {

    private static final String FIRST_NAME = "Yaniv";
    private static final String LAST_NAME = "Nahoum";
    @Autowired private MockMvc mockMvc;

    @Test
    void givenControllerWithRequestParams_whenRequestIsMade_thenArgumentsAreMappedCorrectly() throws Exception {
        mockMvc.perform(get("/getWithRequestParams?firstName={firstName}&surname={lastName}", FIRST_NAME, LAST_NAME))
                .andExpect(status().isOk())
                .andExpect(content().string(FIRST_NAME + " " + LAST_NAME));
    }

    @Test
    void givenControllerWithPojoContainingParamsAsFields_whenRequestIsMade_thenArgumentsAreMappedCorrectly() throws Exception {
        mockMvc.perform(get("/getWithPojo?firstName={firstName}&surname={lastName}", FIRST_NAME, LAST_NAME))
                .andExpect(status().isOk())
                .andExpect(content().string(FIRST_NAME + " " + LAST_NAME));
    }
}

@RestController
class SimpleController {

    @GetMapping("getWithRequestParams")
    public String getSomething(String firstName, @RequestParam("surname") String lastName) {
        return firstName + " " + lastName;
    }

    @GetMapping("getWithPojo")
    public String getSomething(Params params) {
        return params.getFirstName() + " " + params.getSurname();
    }
}

@Data
class Params {
    private String firstName;
    // @RequestParam applicable to methods parameters only
    private String surname;
}

