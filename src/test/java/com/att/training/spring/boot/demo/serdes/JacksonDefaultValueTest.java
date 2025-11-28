package com.att.training.spring.boot.demo.serdes;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonDefaultValueTest {
    private static final String EMPTY_JSON = "{}";
    private static final String NULL_VALUE_JSON = """
            { "value": null }
            """;
    @Autowired
    private JsonMapper jsonMapper;

    @Nested
    class RecordWithNoDefault {
        @Test
        void givenRecordWithNoDefault_thenMissingFieldIsNull() throws JacksonException {
            var somePojo = jsonMapper.readValue(EMPTY_JSON, SomePojo.class);
            assertThat(somePojo.value()).isNull();
        }

        @Test
        void givenRecordWithNoDefault_thenNullFieldIsNull() throws JacksonException {
            var somePojo = jsonMapper.readValue(NULL_VALUE_JSON, SomePojo.class);
            assertThat(somePojo.value()).isNull();
        }

        record SomePojo(String value) {}
    }

    @Nested
    class RecordWithDefault {
        @Test
        void givenRecordWithDefault_thenMissingFieldEqualsDefault() throws JacksonException {
            var somePojoWithDefault = jsonMapper.readValue("{}", SomePojoWithDefault.class);
            assertThat(somePojoWithDefault.value()).isEqualTo("default");
        }


        @Test
        void givenRecordWithDefault_thenNullFieldEqualsDefault() throws JacksonException {
            var somePojoWithDefault = jsonMapper.readValue(NULL_VALUE_JSON, SomePojoWithDefault.class);
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
        @Disabled("Waiting until https://github.com/projectlombok/lombok/issues/3950 is resolved")
        @Test
        void givenBuilderRecordWithDefault_thenMissingFieldEqualsDefault() throws JacksonException {
            var someLombokBuilderPojo = jsonMapper.readValue(EMPTY_JSON, SomeLombokBuilderPojo.class);
            assertThat(someLombokBuilderPojo.value()).isEqualTo("default");
        }

        @Test
        void givenBuilderRecordWithDefault_thenNullFieldEqualsNull() throws JacksonException {
            var someLombokBuilderPojo = jsonMapper.readValue(NULL_VALUE_JSON, SomeLombokBuilderPojo.class);
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
