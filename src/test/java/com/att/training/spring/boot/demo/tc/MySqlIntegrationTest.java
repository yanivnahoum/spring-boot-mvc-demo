package com.att.training.spring.boot.demo.tc;

import com.att.training.spring.boot.demo.Slow;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slow
@SpringBootTest
@ContextConfiguration(initializers = MySqlIntegrationTest.Initializer.class)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
public abstract class MySqlIntegrationTest {

    private static final String[] options = {
            "--character-set-server=latin1",
            "--collation-server=latin1_general_ci",
            "--log-bin-trust-function-creators=true"
    };

    @Container
    private static final MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0.19")
            .withDatabaseName("demo")
            .withCreateContainerCmdModifier(cmd -> cmd.withCmd(options));

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.datasource.url=" + mySqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mySqlContainer.getUsername(),
                    "spring.datasource.password=" + mySqlContainer.getPassword()
            );
            values.applyTo(applicationContext);
        }
    }
}
