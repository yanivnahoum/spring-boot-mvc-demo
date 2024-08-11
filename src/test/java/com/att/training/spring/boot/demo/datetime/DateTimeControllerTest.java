package com.att.training.spring.boot.demo.datetime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest(DateTimeController.class)
class DateTimeControllerTest {

    private static final Instant INSTANT = LocalDate.of(2000, 1, 1)
                                                    .atStartOfDay(UTC)
                                                    .toInstant();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private Clock fixedClock;

    //@BeforeEach
    void setUp() {
        when(fixedClock.instant()).thenReturn(INSTANT);
        when(fixedClock.getZone()).thenReturn(UTC);
    }

    @Test
    void whenGetJsr310_then200OkWithCorrectValues() throws Exception {
        var expectedJson = "{'instant': '2000-01-01T00:00:00Z','localDateTime': '2000-01-01T00:00:00'}";
        mockMvc.perform(get("/jsr310"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().json(expectedJson));
    }

    @Test
    void whenPostJsr310_then202Accepted() throws Exception {
        var jsr310 = "{\"instant\": \"2000-01-01T00:00:00Z\",\"localDateTime\": \"2000-01-01T00:00:00\"}";
        mockMvc.perform(post("/jsr310")
                .contentType(APPLICATION_JSON)
                .content(jsr310))
               .andDo(print())
               .andExpect(status().isAccepted());
    }


    @Test
    void wrong() {
        var completedFuture = CompletableFuture.completedFuture(null);
        IntStream.range(0, 1000)
                 .forEach(i -> completedFuture.thenRun(() -> System.out.println(String.format("Processing: %d on thread %s", i, currentThreadName()))));
        System.out.println("Waiting...."); // This will happen only AFTER we're done executing all tasks
        completedFuture.thenRun(() -> System.out.println("Done!"));
    }

    @Test
    void correct() {

        List<Runnable> tasks = IntStream.range(0, 100)
                                        .mapToObj(this::toRunnable)
                                        .collect(toList());
        var future = CompletableFuture.runAsync(() -> tasks.forEach(Runnable::run));
        System.out.println("Waiting...."); // This will happen almost immediately. We can return the future at this point
        future.join();
    }

    private Runnable toRunnable(int i) {
        return () -> System.out.println(String.format("Processing: %d on thread %s", i, currentThreadName()));
    }

    private String currentThreadName() {
        return Thread.currentThread().getName();
    }
}