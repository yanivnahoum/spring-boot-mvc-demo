package com.att.training.spring.boot.demo.tc;


import com.att.training.spring.boot.demo.Slow;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Slow
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:mysql:8.0.31:////demo",
        // Required for versions of Spring Boot < 2.3.0
        //"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
@Transactional
public @interface SpringBootMySqlTest {
}
