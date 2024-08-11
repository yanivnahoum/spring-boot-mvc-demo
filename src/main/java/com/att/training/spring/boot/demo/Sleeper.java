package com.att.training.spring.boot.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
@Slf4j
class Sleeper {

    private final Random random;

    public Sleeper() {
        random = new Random();
    }

    public void sleepRandom(int minMs, int maxMs) {
        int diff = maxMs - minMs;
        int delay = random.nextInt(diff) + minMs;
        log.debug("#sleepRandom - sleeping {}ms...", delay);
        sleepUninterruptibly(delay, MILLISECONDS);
    }
}
