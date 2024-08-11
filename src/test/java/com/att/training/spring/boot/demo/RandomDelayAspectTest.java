package com.att.training.spring.boot.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class RandomDelayAspectTest {

    private static final int MIN_DELAY_AT_CLASS_LEVEL = 100;
    private static final int MAX_DELAY_AT_CLASS_LEVEL = 200;
    private static final int MIN_DELAY_AT_METHOD_LEVEL = 300;
    private static final int MAX_DELAY_AT_METHOD_LEVEL = 400;

    @Autowired
    private TargetClass targetClass;
    @MockBean
    private Sleeper sleeper;

    @Test
    void givenClassDelay_whenTargetExecuted_shouldSleepWithCorrectArgs() {
        targetClass.nonAnnotatedMethod();
        verify(sleeper).sleepRandom(MIN_DELAY_AT_CLASS_LEVEL, MAX_DELAY_AT_CLASS_LEVEL);
    }

    @Test
    void givenMethodDelay_whenTargetExecuted_shouldSleepWithCorrectArgs() {
        targetClass.annotatedMethod();
        verify(sleeper).sleepRandom(MIN_DELAY_AT_METHOD_LEVEL, MAX_DELAY_AT_METHOD_LEVEL);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy
    @Import({RandomDelayAspect.class, TargetClass.class})
    static class Config {
    }

    @RandomDelay(min = MIN_DELAY_AT_CLASS_LEVEL, max = MAX_DELAY_AT_CLASS_LEVEL)
    static class TargetClass {

        void nonAnnotatedMethod() {
        }

        @RandomDelay(min = MIN_DELAY_AT_METHOD_LEVEL, max = MAX_DELAY_AT_METHOD_LEVEL)
        void annotatedMethod() {
        }
    }
}