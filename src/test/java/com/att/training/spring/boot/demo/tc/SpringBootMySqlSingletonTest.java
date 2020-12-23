package com.att.training.spring.boot.demo.tc;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@SpringBootTest
@Transactional
// Placing this on MySqlTestConfig itself doesn't get picked up by Spring. Neither does @DynamicPropertySource
@ContextConfiguration(classes = MySqlTestConfig.class, initializers = MySqlTestConfig.Initializer.class)
public @interface SpringBootMySqlSingletonTest {
}
