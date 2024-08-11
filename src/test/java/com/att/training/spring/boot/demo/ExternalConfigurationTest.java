package com.att.training.spring.boot.demo;

import com.att.training.spring.boot.demo.user.ExternalUserProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExternalConfigurationTest {

    @Autowired
    private ExternalUserProperties config;
    @Autowired
    private Environment env;

    @Test
    void externalConfigurationCanBeReadFromClasspathOrFromDisk() {
        var configDir = env.getProperty("app.config.dir");
        int expectedUserCount = configDir == null ? 3 : 2;
        assertThat(config).isNotNull();
        assertThat(config.getUsers()).hasSize(expectedUserCount);
    }

    @EnableConfigurationProperties(ExternalUserProperties.class)
    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {
    }
}

