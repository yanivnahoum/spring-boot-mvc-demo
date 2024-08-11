package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.util.Map;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

@JsonTest
class JacksonDeserializationOfDynamicObjectsTest {
    private static final String SIMPLE_JSON = "{ 'name': 'John', 'data': { 'k1': 'v1', 'k2': 'v2' } }";
    private static final String NESTED_JSON = "{ 'name': 'John', 'data': { 'k1': { 'k2': 'v2' } } }";
    @Autowired private ObjectMapper mapper;

    @Nested
    @DisplayName("Deserialize json to generic Map<String, Object>")
    class MapTest {

        @Test
        void simpleMapValueIsDeserializedCorrectly() throws JsonProcessingException {
            var somePojoWithMap = mapper.readValue(singleToDoubleQuotes(SIMPLE_JSON), SomePojoWithMap.class);

            assertThat(somePojoWithMap.getName()).isEqualTo("John");
            assertThat(somePojoWithMap.getData())
                    .containsEntry("k1", "v1")
                    .containsEntry("k2", "v2");
        }

        @Test
        void nestedMapValueIsDeserializedCorrectly() throws JsonProcessingException {
            var somePojoWithMap = mapper.readValue(singleToDoubleQuotes(NESTED_JSON), SomePojoWithMap.class);

            assertThat(somePojoWithMap.getName()).isEqualTo("John");
            assertThat(somePojoWithMap.getData())
                    .extracting("k1")
                    .asInstanceOf(MAP)
                    .containsEntry("k2", "v2");
        }
    }

    @Nested
    @DisplayName("Deserialize json to JsonNode")
    class JsonNodeTest {

        @Test
        void simpleMapValueIsDeserializedCorrectly() throws JsonProcessingException {
            var somePojoWithJsonNode = mapper.readValue(singleToDoubleQuotes(SIMPLE_JSON), SomePojoWithJsonNode.class);

            assertThat(somePojoWithJsonNode.getName()).isEqualTo("John");
            assertThat(somePojoWithJsonNode.getData().get("k1").asText()).isEqualTo("v1");
            var simpleValue = somePojoWithJsonNode.getData()
                    .get("k2").asText();
            assertThat(simpleValue).isEqualTo("v2");
        }

        @Test
        void nestedMapValueIsDeserializedCorrectly() throws JsonProcessingException {
            var somePojoWithJsonNode = mapper.readValue(singleToDoubleQuotes(NESTED_JSON), SomePojoWithJsonNode.class);

            assertThat(somePojoWithJsonNode.getName()).isEqualTo("John");
            var nestedValue = somePojoWithJsonNode.getData()
                    .get("k1")
                    .get("k2").asText();
            assertThat(nestedValue).isEqualTo("v2");

            var pathValue = somePojoWithJsonNode.getData()
                    .at("/k1/k2").asText();
            assertThat(pathValue).isEqualTo("v2");
        }
    }

    @Value
    static class SomePojoWithMap {
        String name;
        Map<String, Object> data;
    }

    @Value
    static class SomePojoWithJsonNode {
        String name;
        JsonNode data;
    }
}
