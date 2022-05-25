package com.att.training.spring.boot.demo.tc;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
public class MySqlTestConfig {

    private static final String[] options = {
            "--character-set-server=latin1",
            "--collation-server=latin1_general_ci",
            "--log-bin-trust-function-creators=true"
    };
    private static final MySQLContainer<?> mySqlContainer = createAndStart();

    private static MySQLContainer<?> createAndStart() {
        var container = new MySQLContainer<>("mysql:8.0.29")
                .withDatabaseName("demo")
                .withCreateContainerCmdModifier(cmd -> cmd.withCmd(options));
        container.start();
        return container;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.datasource.url=" + mySqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mySqlContainer.getUsername(),
                    "spring.datasource.password=" + mySqlContainer.getPassword()
            );
            values.applyTo(applicationContext);
        }
    }
}
