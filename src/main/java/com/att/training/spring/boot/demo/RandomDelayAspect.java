package com.att.training.spring.boot.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Random;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RandomDelayAspect {

    private final Random random;

    public RandomDelayAspect() {
        random = new Random();
    }

    @Pointcut("within(com.att.training.spring.boot.demo..*)")
    private void inApp() {}

    @Pointcut("@within(randomDelay) && !@annotation(RandomDelay)")
    private void delayedClass(RandomDelay randomDelay) {}

    @Pointcut("@annotation(randomDelay)")
    private void delayedMethod(RandomDelay randomDelay) {}

    @Before("inApp() && delayedClass(randomDelay)")
    public void adviseMethodsOfAnnotatedClass(JoinPoint joinPoint, RandomDelay randomDelay) {
        delayConditionally(joinPoint, randomDelay);
    }

    @Before("inApp() && delayedMethod(randomDelay)")
    public void adviseAnnotatedMethods(JoinPoint joinPoint, RandomDelay randomDelay) {
        delayConditionally(joinPoint, randomDelay);
    }

    private void delayConditionally(JoinPoint joinPoint, RandomDelay randomDelay) {
        if (isValid(randomDelay, joinPoint)) {
            delayExecution(randomDelay);
        }
    }

    private boolean isValid(RandomDelay randomDelay, JoinPoint joinPoint) {
        int diff = getDifference(randomDelay);
        if (diff <= 0) {
            String signature = joinPoint.getSignature().toShortString();
            log.warn("#delayExecution - ignoring @RandomDelay since it has been passed invalid arguments: min must be equal to or greater than zero, and max must be greater than min. See {}", signature);
            return false;
        }
        return true;
    }

    private void delayExecution(RandomDelay randomDelay) {
        int diff = getDifference(randomDelay);
        int delayMs = random.nextInt(diff) + randomDelay.min();
        log.debug("#delayExecution - delaying execution by {}ms", delayMs);
        sleepUninterruptibly(delayMs, MILLISECONDS);
    }

    private int getDifference(RandomDelay randomDelay) {
        return randomDelay.max() - randomDelay.min();
    }
}
