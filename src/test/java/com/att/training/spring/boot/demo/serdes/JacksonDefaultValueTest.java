package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonDefaultValueTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenRecordWithNoDefault_thenMissingFieldIsNull() throws JsonProcessingException {
        var somePojo = objectMapper.readValue("{}", SomePojo.class);
        assertThat(somePojo.value()).isNull();
    }

    @Test
    void givenRecordWithDefault_thenMissingFieldEqualsDefault() throws JsonProcessingException {
        var somePojoWithDefault = objectMapper.readValue("{}", SomePojoWithDefault.class);
        assertThat(somePojoWithDefault.value()).isEqualTo("default");
    }

    @Test
    void givenRecordWithDefault_thenNullFieldEqualsDefault() throws JsonProcessingException {
        var json = """
                { "value": null}
                """;
        var somePojoWithDefault = objectMapper.readValue(json, SomePojoWithDefault.class);
        assertThat(somePojoWithDefault.value()).isEqualTo("default");
    }

    record SomePojo(String value) {}

    record SomePojoWithDefault(String value) {
        SomePojoWithDefault {
            if (value == null) {
                value = "default";
            }
        }
    }
}
