package com.att.training.spring.boot.demo.tc;

import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;

import javax.persistence.EntityManager;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@SpringBootTest
@Import(DatasourceProxyBeanPostProcessor.class)
@TestInstance(PER_CLASS)
@Slf4j
public abstract class MySqlSingletonContainer {

    private static final String[] options = {
            "--character-set-server=latin1",
            "--collation-server=latin1_general_ci",
            "--log-bin-trust-function-creators=true"
    };
    private static final MySQLContainer<?> mySqlContainer = createAndStartDb();
    @Autowired protected EntityManager entityManager;
    @Autowired protected TransactionTemplate transactionTemplate;
    @Autowired protected ProxyTestDataSource testDataSource;

    @DynamicPropertySource
    static void mySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);
    }

    @SuppressWarnings("resource")
    private static MySQLContainer<?> createAndStartDb() {
        var container = new MySQLContainer<>("mysql:8.0.31")
                .withDatabaseName("demo")
                .withCreateContainerCmdModifier(cmd -> cmd.withCmd(options))
//            .withUrlParam("profileSQL", "true")
//            .withUrlParam("rewriteBatchedStatements", "true")
//            .withLogConsumer(new Slf4jLogConsumer(log))
                .withReuse(true);
        container.start();
        return container;
    }

    @BeforeEach
    void beforeEach() {
        testDataSource.reset();
    }

    @AfterAll
    void cleanup() {
        transactionTemplate.executeWithoutResult(status -> {
            for (String table : tablesToDrop()) {
                entityManager.createNativeQuery("DROP TABLE " + table).executeUpdate();
            }
        });
    }

    protected List<String> tablesToDrop() {
        return emptyList();
    }
}



