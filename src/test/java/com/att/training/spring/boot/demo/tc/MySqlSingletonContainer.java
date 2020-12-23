package com.att.training.spring.boot.demo.tc;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@SpringBootTest
@DisableWithoutDocker
public abstract class MySqlSingletonContainer {

    private static final String[] options = {
            "--character-set-server=latin1",
            "--collation-server=latin1_general_ci",
            "--log-bin-trust-function-creators=true"
    };

    private static final MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0.22")
            .withDatabaseName("demo")
            .withCreateContainerCmdModifier(cmd -> cmd.withCmd(options))
            .withReuse(true);
//            .withUrlParam("profileSQL", "true")
//            .withUrlParam("rewriteBatchedStatements", "true");

    static  {
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

@Target({TYPE, METHOD})
@Retention(RUNTIME)
@ExtendWith(DisableWithoutDockerCondition.class)
@Inherited
@interface DisableWithoutDocker {
}

class DisableWithoutDockerCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED_BY_DEFAULT =
            ConditionEvaluationResult.enabled("@DisableWithoutDocker is not present");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return findAnnotation(context.getElement(), DisableWithoutDocker.class)
                .map(__ -> evaluate())
                .orElse(ENABLED_BY_DEFAULT);
    }

    private ConditionEvaluationResult evaluate() {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            return ConditionEvaluationResult.enabled("Docker is available");
        }
        return ConditionEvaluationResult.disabled("disabledWithoutDocker is present and Docker is not available");
    }
}
