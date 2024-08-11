package com.att.training.spring.boot.demo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.lang.Boolean.parseBoolean;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
// Uncomment to have a SyncTaskExecutor replace all Executors, running everything on the main thread.
//@Import(TestConfig.class)
class AsyncControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void testSync() throws Exception {
        mockMvc.perform(get("/sync"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "false" })
    void testAsyncWithError(String withError) throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/async")
                .queryParam("withError", withError))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(parseBoolean(withError) ? status().isInternalServerError(): status().isOk())
                .andDo(print());
    }
}

@TestConfiguration
class TestConfig {
    @Primary
    @Bean
    Executor syncTaskExecutor() {
        return new SyncTaskExecutor();
    }
}

@RestController
@RequiredArgsConstructor
class AsyncController {

    private final AsyncTestRunner runner;

    @GetMapping("sync")
    void runSync() {
        runAll(false).join();
    }

    @GetMapping("async")
    CompletableFuture<Void> runAsync(@RequestParam(defaultValue = "false") boolean withError) {
        return runAll(withError);
    }

    private CompletableFuture<Void> runAll(boolean withError) {
        return CompletableFuture.allOf(runner.foo(), runner.bar(withError));
    }
}

@Slf4j
@Component
class AsyncTestRunner {

    @Async
    @SneakyThrows(InterruptedException.class)
    CompletableFuture<String> foo() {
        log.info("#foo - running");
        SECONDS.sleep(2);
        return CompletableFuture.completedFuture("foo - done");
    }

    @SneakyThrows(InterruptedException.class)
    @Async
    CompletableFuture<String> bar(boolean withError) {
        log.info("#bar - running. withError={}", withError);
        SECONDS.sleep(1);
        if (withError) {
            boom();
        }
        return CompletableFuture.completedFuture("bar - done");
    }

    private void boom() {
        throw new RuntimeException("Boom!");
    }
}
