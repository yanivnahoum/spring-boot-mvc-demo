package com.att.training.spring.boot.demo;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Adds a random delay of x ms (between {@link #min()} and {@link #max()}
 * to the execution of the annotated method or to all methods of the annotated class.
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface RandomDelay {

    /**
     * Must be equal to or greater than zero.
     * @return the minimum delay in milliseconds
     */
    int min() default 0;

    /**
     * Must be greater than {@link #min()}
     * @return the maximum delay in milliseconds
     */
    int max() default 1000;
}
