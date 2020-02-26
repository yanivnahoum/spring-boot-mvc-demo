package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.Slow;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slow
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:mysql:////demo",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
@Testcontainers(disabledWithoutDocker = true)
abstract class MySqlIntegrationTest {

    @Container
    private static final MySQLContainer<?> mySqlContainer = new MySQLContainer<>();
}
