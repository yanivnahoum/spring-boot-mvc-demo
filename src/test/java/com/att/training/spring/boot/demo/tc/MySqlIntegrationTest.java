package com.att.training.spring.boot.demo.tc;

import com.att.training.spring.boot.demo.Slow;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slow
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
public abstract class MySqlIntegrationTest {

    private static final String[] options = {
            "--character-set-server=latin1",
            "--collation-server=latin1_general_ci",
            "--log-bin-trust-function-creators=true"
    };

    @Container
    private static final MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0.20")
            .withDatabaseName("demo")
            .withCreateContainerCmdModifier(cmd -> cmd.withCmd(options));

    static {
        // This is only needed because of spring-cloud-contract-wiremock's WireMockTestExecutionListener
        // It causes the application context to load (and activates #mySqlProperties()) before the container is ready
        // It's also needed in case you're using @TestContainers/@Container with junit-jupiter's @TestInstance(PER_CLASS)
        mySqlContainer.start();
    }

    @DynamicPropertySource
    static void mySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);
    }
}
