package com.att.training.spring.boot.demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LombokTest {

    @Autowired private ClassWithCopyableAnnotationsOnFields testClass;

    @DisplayName("@Value and @Qualifier on fields should be copied by lombok to c'tor parameters")
    @Test
    void testCopyableAnnotations() {
        assertThat(testClass.getValue()).isEqualTo("defaultValue");
        assertThat(testClass.getSomePojoA().getId()).isEqualTo(1);
        assertThat(testClass.getSomePojoB().getId()).isEqualTo(2);
    }

    @Configuration(proxyBeanMethods = false)
    @Import(ClassWithCopyableAnnotationsOnFields.class)
    static class Config{

        @Bean
        @Qualifier("pojo1")
        SomePojo somePojo1() {
            return new SomePojo(1);
        }

        @Bean
        @Qualifier("pojo2")
        SomePojo somePojo2() {
            return new SomePojo(2);
        }
    }
}

@RequiredArgsConstructor
@Getter
class SomePojo{
    private final int id;
}

@TestComponent
@RequiredArgsConstructor
@Getter
class ClassWithCopyableAnnotationsOnFields {
    @Value("${some.missing.value:defaultValue}")
    private final String value;
    @Qualifier("pojo1")
    private final SomePojo somePojoA;
    @Qualifier("pojo2")
    private final SomePojo somePojoB;
}
