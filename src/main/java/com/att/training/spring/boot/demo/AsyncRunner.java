package com.att.training.spring.boot.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
class AsyncRunner {
    @Async
    void runAsync(String[] args) {
        log.info("#runAsync - running with args: {}", Arrays.toString(args));
    }
}
