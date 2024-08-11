package com.att.training.spring.boot.demo;

import com.att.training.spring.boot.demo.user.ExternalUserConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest
class ExternalConfigurationTest {

    @Autowired
    private ExternalUserConfiguration config;

    @Test
    void foo() {
        assertThat(config).isNotNull();
        assertThat(config.getUsers()).hasSize(3);
    }

    @EnableConfigurationProperties(ExternalUserConfiguration.class)
    @Configuration
    static class TestConfiguration {
    }
}
