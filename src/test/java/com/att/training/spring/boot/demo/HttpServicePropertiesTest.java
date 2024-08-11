package com.att.training.spring.boot.demo;

import com.att.training.spring.boot.demo.user.HttpServiceProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = HttpServicePropertiesTest.TestConfiguration.class)
@TestPropertySource(properties = "app.http-service.url=https://some-domain.com:8080")
class HttpServicePropertiesTest {

    @EnableConfigurationProperties(HttpServiceProperties.class)
    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {}

    @TestPropertySource(properties = {
            "app.http-service.connect-timeout=5s",
            "app.http-service.read-timeout=10s",
            "app.http-service.max-retries=3",
    })
    @Nested
    class WithSpecifiedValues {

        @Autowired
        private HttpServiceProperties httpServiceProperties;

        @Test
        void immutablePojosCanBeUsedAsConfigurationProperties() {
            var expectedProperties = new HttpServiceProperties("https://some-domain.com:8080", Duration.ofSeconds(5),
                    Duration.ofSeconds(10), 3);
            assertThat(httpServiceProperties).isEqualTo(expectedProperties);
        }
    }

    @Nested
    class WithDefaultValues {

        @Autowired
        private HttpServiceProperties httpServiceProperties;

        @Test
        void immutablePojosCanBeUsedAsConfigurationProperties() {
            var expectedProperties = new HttpServiceProperties("https://some-domain.com:8080", Duration.ofSeconds(30),
                    Duration.ofSeconds(30), 5);
            assertThat(httpServiceProperties).isEqualTo(expectedProperties);
        }
    }
}
