package com.att.training.spring.boot.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
class AsyncRunner1 {
    // Takes the default Executor names "taskExecutor"
    @Async
    void runAsync(String[] args) {
        log.info("#runAsync1 - running with args: {}", Arrays.toString(args));
    }
}

@Component
@Slf4j
class AsyncRunner2 {
    // We can choose a different thread-pool
    @Async("ioTaskExecutor")
    void runAsync(String[] args) {
        log.info("#runAsync2 - running with args: {}", Arrays.toString(args));
    }
}
