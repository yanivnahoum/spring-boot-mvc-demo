package com.att.training.spring.boot.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RandomDelayAspect {

    private final Sleeper sleeper;

    @Pointcut("within(com.att.training.spring.boot.demo..*)")
    void inApp() {
    }

    @Pointcut("@within(randomDelay) && !@annotation(RandomDelay)")
    void delayedClass(RandomDelay randomDelay) {
    }

    @Pointcut("@annotation(randomDelay)")
    void delayedMethod(RandomDelay randomDelay) {
    }

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
            log.warn("#isValid - ignoring @RandomDelay since it has been passed invalid arguments: min must be equal " +
                    "to or greater than zero, and max must be greater than min. See {}", signature);
            return false;
        }
        return true;
    }

    private void delayExecution(RandomDelay randomDelay) {
        sleeper.sleepRandom(randomDelay.min(), randomDelay.max());
    }

    private int getDifference(RandomDelay randomDelay) {
        return randomDelay.max() - randomDelay.min();
    }
}
