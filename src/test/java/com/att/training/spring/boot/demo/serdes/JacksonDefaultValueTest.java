package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonDefaultValueTest {
    private static final String EMPTY_JSON = "{}";
    private static final String NULL_VALUE_JSON = """
            { "value": null }
            """;
    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class RecordWithNoDefault {
        @Test
        void givenRecordWithNoDefault_thenMissingFieldIsNull() throws JsonProcessingException {
            var somePojo = objectMapper.readValue(EMPTY_JSON, SomePojo.class);
            assertThat(somePojo.value()).isNull();
        }

        @Test
        void givenRecordWithNoDefault_thenNullFieldIsNull() throws JsonProcessingException {
            var somePojo = objectMapper.readValue(NULL_VALUE_JSON, SomePojo.class);
            assertThat(somePojo.value()).isNull();
        }

        record SomePojo(String value) {}
    }

    @Nested
    class RecordWithDefault {
        @Test
        void givenRecordWithDefault_thenMissingFieldEqualsDefault() throws JsonProcessingException {
            var somePojoWithDefault = objectMapper.readValue("{}", SomePojoWithDefault.class);
            assertThat(somePojoWithDefault.value()).isEqualTo("default");
        }


        @Test
        void givenRecordWithDefault_thenNullFieldEqualsDefault() throws JsonProcessingException {
            var somePojoWithDefault = objectMapper.readValue(NULL_VALUE_JSON, SomePojoWithDefault.class);
            assertThat(somePojoWithDefault.value()).isEqualTo("default");
        }

        record SomePojoWithDefault(String value) {
            SomePojoWithDefault {
                if (value == null) {
                    value = "default";
                }
            }
        }
    }

    @Nested
    class RecordWithLombokBuilder {
        @Test
        void givenBuilderRecordWithDefault_thenMissingFieldEqualsDefault() throws JsonProcessingException {
            var someLombokBuilderPojo = objectMapper.readValue(EMPTY_JSON, SomeLombokBuilderPojo.class);
            assertThat(someLombokBuilderPojo.value()).isEqualTo("default");
        }

        @Test
        void givenBuilderRecordWithDefault_thenNullFieldEqualsNull() throws JsonProcessingException {
            var someLombokBuilderPojo = objectMapper.readValue(NULL_VALUE_JSON, SomeLombokBuilderPojo.class);
            assertThat(someLombokBuilderPojo.value()).isNull();
        }

        @Jacksonized
        @Builder
        record SomeLombokBuilderPojo(String value) {
            @SuppressWarnings({"unused", "FieldMayBeFinal"})
            public static class SomeLombokBuilderPojoBuilder {
                private String value = "default";
            }
        }
    }
}
