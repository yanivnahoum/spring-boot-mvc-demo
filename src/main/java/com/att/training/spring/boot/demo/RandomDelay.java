package com.att.training.spring.boot.demo;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface RandomDelay {

    /**
     * Must be equal to or greater than zero.
     * @return the minimum delay
     */
    int min() default 0;

    /**
     * Must be greater than {@link #min()}
     * @return the minimum delay
     */
    int max() default 1000;
}
