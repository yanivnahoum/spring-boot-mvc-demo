package com.att.training.spring.boot.demo.user;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:mysql://host:3306/demo",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
@Testcontainers
abstract class MySqlIntegrationTest {

    private static final MySQLContainer mySqlContainer = new MySQLContainer();

}
