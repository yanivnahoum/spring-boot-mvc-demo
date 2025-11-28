package com.att.training.spring.boot.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class AsyncRunner2 {
    // We can choose a different thread-pool
    @Async("cpuTaskExecutor")
    public void runAsync(String[] args) {
        log.info("#runAsync2 - running with args: {}", Arrays.toString(args));
    }
}
