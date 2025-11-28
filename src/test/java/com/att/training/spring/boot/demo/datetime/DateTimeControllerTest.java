package com.att.training.spring.boot.demo.datetime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(DateTimeController.class)
class DateTimeControllerTest {

    // Canonical expected JSON (text block per guidelines)
    private static final String EXPECTED_JSON = """
            {
              "instant": "2000-01-01T00:00:00Z",
              "localDateTime": "2000-01-01T00:00:00"
            }
            """;

    private static final Instant INSTANT = LocalDate.of(2000, 1, 1)
            .atStartOfDay(UTC)
            .toInstant();

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        when(fixedClock.instant()).thenReturn(INSTANT);
        when(fixedClock.getZone()).thenReturn(UTC);
    }

    @Test
    void whenGetJsr310_then200OkWithCorrectValues() {
        // Arrange & Act
        MvcTestResult result = mvc.get().uri("/jsr310").exchange();
        // Assert
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo(EXPECTED_JSON);
    }

    @Test
    void whenGetJsr310WithQueryParamsInDefaultFormat_then200OkWithCorrectValues() {
        MvcTestResult result = mvc.get()
                .uri("/jsr310/v2")
                .param("instant", "2000-01-01T00:00:00Z")
                .param("datetime", "2000-01-01T00:00")
                .exchange();
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo(EXPECTED_JSON);
    }

    @Test
    void whenGetJsr310WithQueryParamsInISOFormat_then200OkWithCorrectValues() {
        MvcTestResult result = mvc.get()
                .uri("/jsr310/v3")
                .param("instant", "2000-01-01T00:00:00Z")
                .param("datetime", "2000-01-01T00:00:00")
                .exchange();
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo(EXPECTED_JSON);
    }

    @Test
    void whenGetJsr310WithQueryParamsInCustomFormat_then200OkWithCorrectValues() {
        // Arrange & Act (space left unencoded so formatter pattern "dd-MM-yyyy HH:mm:ss" matches when decoded)
        MvcTestResult result = mvc.get()
                .uri("/jsr310/v4")
                .param("instant", "2000-01-01T00:00:00Z")
                .param("datetime", "01-01-2000 00:00:00")
                .exchange();
        // Assert
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo(EXPECTED_JSON);
    }

    @Test
    void whenPostJsr310_then202Accepted() {
        MvcTestResult result = mvc.post().uri("/jsr310")
                .contentType(APPLICATION_JSON)
                .content(EXPECTED_JSON)
                .exchange();
        assertThat(result)
                .hasStatus(HttpStatus.ACCEPTED);
    }

    @Test
    void whenGetCustomJsr310_then200OkWithCorrectDeserializationUsingCustomFormat() {
        String expectedCustomJson = """
                {
                  "instant": "2000-01-01T00:00:00Z",
                  "localDateTime": "01-01-2000 00:00:00"
                }
                """;
        MvcTestResult result = mvc.get().uri("/jsr310/custom").exchange();
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo(expectedCustomJson);
    }
}
