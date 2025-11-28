package com.att.training.spring.boot.demo.serdes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.mrbean.MrBeanModule;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonDeserializationWithInterfacesTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        JsonMapperBuilderCustomizer customizer() {
            return builder -> builder.addModule(new MrBeanModule());
        }
    }

    @Autowired
    private JsonMapper mapper;

    @Test
    void deserializingAnInterface_shouldSucceed() throws JacksonException {
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
