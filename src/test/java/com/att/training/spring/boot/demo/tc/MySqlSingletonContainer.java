package com.att.training.spring.boot.demo.tc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
public abstract class MySqlSingletonContainer {

    private static final String[] options = {
            "--character-set-server=latin1",
            "--collation-server=latin1_general_ci",
            "--log-bin-trust-function-creators=true"
    };

    private static final MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0.22")
            .withDatabaseName("demo")
            .withCreateContainerCmdModifier(cmd -> cmd.withCmd(options))
//            .withUrlParam("profileSQL", "true")
            .withReuse(true);
//            .withUrlParam("rewriteBatchedStatements", "true");

    static {
        // At the end of the test suite the Ryuk container that is started by Testcontainers
        // core will take care of stopping the singleton container.
        mySqlContainer.start();
    }

    @DynamicPropertySource
    static void mySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);
    }
}