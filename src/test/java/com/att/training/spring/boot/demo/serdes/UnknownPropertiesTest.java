package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest(properties = "spring.jackson.deserialization.fail-on-unknown-properties=true")
class UnknownPropertiesTest {
    @Autowired
    private ObjectMapper objectMapper;

    // We can change jackson's behavior and ignore unknown properties.
    // It doesn't work the other way around: if we set fail-on-unknown-properties=false
    // (configured by default in Spring Boot), adding @JsonIgnoreProperties(ignoreUnknown = false) won't make it fail.
    @JsonIgnoreProperties(ignoreUnknown = true)
    record LiberalUser(int id, String name) {}

    record StrictUser(int id, String name) {}

    @Test
    void liberal() throws JsonProcessingException {
        String json = """
                {
                    "id": 1,
                    "name": "John",
                    "age": 30
                }
                """;
        var user = objectMapper.readValue(json, LiberalUser.class);
        assertThat(user).isEqualTo(new LiberalUser(1, "John"));
    }

    @Test
    void strict() {
        String json = """
                {
                    "id": 1,
                    "name": "John",
                    "age": 30
                }
                """;
        assertThatThrownBy(() -> objectMapper.readValue(json, StrictUser.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Unrecognized field \"age\"");
    }
}
