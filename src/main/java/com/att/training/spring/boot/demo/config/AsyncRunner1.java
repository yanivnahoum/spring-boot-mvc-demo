package com.att.training.spring.boot.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class AsyncRunner1 {
    // Takes the default Executor or an executor named "taskExecutor"
    @Async
    public void runAsync(String[] args) {
        log.info("#runAsync1 - running with args: {}", Arrays.toString(args));
    }
}
