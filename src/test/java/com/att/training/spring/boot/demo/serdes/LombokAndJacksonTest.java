package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;

class LombokAndJacksonTest {
    // Create an ObjectMapper adding the ParameterNamesModule, or inject Spring's auto-configured one.
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new ParameterNamesModule())
            .build();

    @DisplayName("Most immutable pojos only require that the -parameters compiler arg is specified and that the " +
            "ParameterNamesModule is added to the ObjectMapper")
    @Test
    void deserializeImmutablePojo() throws JsonProcessingException {
        String json = singleToDoubleQuotes("{ 'x': '1', 'y': '2' }");
        ImmutablePojo pojo = objectMapper.readValue(json, ImmutablePojo.class);
        assertThat(pojo).usingRecursiveComparison()
                .isEqualTo(new ImmutablePojo(1, 2));
    }

    @DisplayName("Immutable pojos with a single field created using lombok's @Value need a special annotation. " +
            "See lombok.config: lombok.anyConstructor.addConstructorProperties")
    @Test
    void deserializeImmutablePojoWithSingleField() throws JsonProcessingException {
        String json = singleToDoubleQuotes("{ 'value': '10' }");
        ImmutablePojoWithSingleField pojo = objectMapper.readValue(json, ImmutablePojoWithSingleField.class);
        assertThat(pojo.getValue()).isEqualTo(10);
    }

    @Value
    static class ImmutablePojo {
        int x;
        int y;
    }

    @Value
    static class ImmutablePojoWithSingleField {
        int value;
    }
}
