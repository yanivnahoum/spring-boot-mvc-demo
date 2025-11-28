package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest(properties = "spring.jackson.deserialization.fail-on-unknown-properties=true")
class UnknownPropertiesTest {
    @Autowired
    private JsonMapper jsonMapper;

    // We can change jackson3's behavior and fail on unknow properties (as was the default in Jackson2).
    // It doesn't work the other way around: if we set fail-on-unknown-properties=false
    // (configured by default in Spring Boot), adding @JsonIgnoreProperties(ignoreUnknown = false) won't make it fail.
    @JsonIgnoreProperties(ignoreUnknown = true)
    record LiberalUser(int id, String name) {}

    record StrictUser(int id, String name) {}

    @Test
    void liberal() throws JacksonException {
        String json = """
                {
                    "id": 1,
                    "name": "John",
                    "age": 30
                }
                """;
        var user = jsonMapper.readValue(json, LiberalUser.class);
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
        assertThatThrownBy(() -> jsonMapper.readValue(json, StrictUser.class))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("""
                        Unrecognized property "age"\
                        """);
    }
}
