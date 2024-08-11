package com.att.training.spring.boot.demo.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExternalConfigurationTest {

    @Autowired
    private ExternalUserConfiguration config;

    @Test
    void foo() {
        System.out.println(config);
        assertThat(config).isNotNull();
        assertThat(config.getUsers()).hasSize(3);
    }
}
