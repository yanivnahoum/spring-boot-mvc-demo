package com.att.training.spring.boot.demo.tc;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@SpringBootTest(properties = "spring.datasource.url=jdbc:tc:mysql:8.0.22:////demo")
@Transactional
public @interface SpringBootMySqlTest {
}
