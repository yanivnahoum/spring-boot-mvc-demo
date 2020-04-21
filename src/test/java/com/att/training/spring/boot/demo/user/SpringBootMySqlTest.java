package com.att.training.spring.boot.demo.user;


import com.att.training.spring.boot.demo.Slow;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Slow
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:mysql:8.0.19:////demo",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
public @interface SpringBootMySqlTest {
}
