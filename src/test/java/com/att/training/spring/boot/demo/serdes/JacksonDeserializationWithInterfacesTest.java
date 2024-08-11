package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonDeserializationWithInterfacesTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        Jackson2ObjectMapperBuilderCustomizer customizer() {
            return builder -> builder.modules(new MrBeanModule());
        }
    }

    @Autowired private ObjectMapper mapper;

    @Test
    void deserializingAnInterface_shouldSucceed() throws JsonProcessingException {
        String json = singleToDoubleQuotes("{ 'firstName': 'John', 'lastName': 'Doe', 'age': '30' }");
        Person person = mapper.readValue(json, Person.class);
        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(person.getLastName()).isEqualTo("Doe");
        assertThat(person.getAge()).isEqualTo(30);
    }

    public interface Person {
        String getFirstName();

        String getLastName();

        int getAge();
    }
}
