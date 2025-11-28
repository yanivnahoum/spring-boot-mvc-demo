package com.att.training.spring.boot.demo.serdes;

import lombok.Data;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonNullOrMissingPropertiesTest {
    @Autowired
    private JsonMapper jsonMapper;

    @Nested
    class UsingOptionalFields {
        @Test
        void givenExistingOrNullOrMissingJsonFields_thenOptionalFieldsShouldBeSetToValueOrEmptyOrLeftNull() throws JacksonException {
            var json = """
                    {
                        "valueField": "hello",
                        "nullField": null
                    }
                    """;
            SomePojo somePojo = jsonMapper.readValue(json, SomePojo.class);
            assertThat(somePojo.getValueField()).hasValue("hello");
            // nullField is specified, but has a null value. It will be deserialized to an empty Optional
            assertThat(somePojo.getNullField()).isEmpty();
            // missingField is missing from the json, and so will have its default null value
            assertThat(somePojo.getMissingField()).isNull();
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @Data
        private static class SomePojo {
            private Optional<String> valueField;
            private Optional<String> nullField;
            private Optional<String> missingField;
        }
    }

    @Nested
    class UsingJsonNode {
        @Test
        void updatingPojoUsingReaderForUpdating() {
            var somePojo = new SomePojo();
            somePojo.setValueField("goodbye");
            somePojo.setNullField("to-be-removed");
            somePojo.setMissingField("to-be-left-untouched");
            var json = """
                    {
                        "valueField": "hello",
                        "nullField": null
                    }
                    """;

            JsonNode jsonNode = jsonMapper.readTree(json);
            SomePojo updatedPojo = jsonMapper.readerForUpdating(somePojo)
                    .readValue(jsonNode);
            // valueField is specified, and so will be updated
            assertThat(updatedPojo.getValueField()).isEqualTo("hello");
            // nullField is specified but has a null value. Its current value will be updated to null
            assertThat(updatedPojo.getNullField()).isNull();
            // missingField is missing from the json, and so will remain untouched
            assertThat(updatedPojo.getMissingField()).isEqualTo("to-be-left-untouched");
        }

        @Data
        private static class SomePojo {
            private String valueField;
            private String nullField;
            private String missingField;
        }
    }
}
