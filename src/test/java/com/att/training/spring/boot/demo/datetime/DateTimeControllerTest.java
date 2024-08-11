package com.att.training.spring.boot.demo.datetime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DateTimeController.class)
class DateTimeControllerTest {

    private static final String EXPECTED_JSON = "{'instant': '2000-01-01T00:00:00Z','localDateTime': '2000-01-01T00:00:00'}";
    private static final Instant INSTANT = LocalDate.of(2000, 1, 1)
                                                    .atStartOfDay(UTC)
                                                    .toInstant();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        when(fixedClock.instant()).thenReturn(INSTANT);
        when(fixedClock.getZone()).thenReturn(UTC);
    }

    @Test
    void whenGetJsr310_then200OkWithCorrectValues() throws Exception {
        mockMvc.perform(get("/jsr310"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().json(EXPECTED_JSON));
    }

    @Test
    void whenGetJsr310WithQueryParamsInDefaultFormat_then200OkWithCorrectValues() throws Exception {
        mockMvc.perform(get("/jsr310/v2")
                .param("instant", "2000-01-01T00:00:00Z")
                .param("datetime", "1/1/00, 12:00 AM"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().json(EXPECTED_JSON));
    }

    @Test
    void whenGetJsr310WithQueryParamsInISOFormat_then200OkWithCorrectValues() throws Exception {
        mockMvc.perform(get("/jsr310/v3")
                .param("instant", "2000-01-01T00:00:00Z")
                .param("datetime", "2000-01-01T00:00:00"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().json(EXPECTED_JSON));
    }

    @Test
    void whenGetJsr310WithQueryParamsInCustomFormat_then200OkWithCorrectValues() throws Exception {
        mockMvc.perform(get("/jsr310/v4")
                .param("instant", "2000-01-01T00:00:00Z")
                .param("datetime", "01-01-2000 00:00:00"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().json(EXPECTED_JSON));
    }

    @Test
    void whenPostJsr310_then202Accepted() throws Exception {
        var jsr310 = EXPECTED_JSON.replace('\'', '"');
        mockMvc.perform(post("/jsr310")
                .contentType(APPLICATION_JSON)
                .content(jsr310))
               .andDo(print())
               .andExpect(status().isAccepted());
    }
}