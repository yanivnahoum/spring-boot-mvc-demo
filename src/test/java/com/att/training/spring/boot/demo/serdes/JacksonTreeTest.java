package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class JacksonTreeTest {
    private static final JsonPointer PERSON_NAME_FIELD_POINTER = JsonPointer.compile("/person/name");
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new ParameterNamesModule())
            .build();

    @Test
    void putAllProperties() throws JsonProcessingException {
        var currentJson = """
                {"x": "100", "propertyToUpdate": { "a": "1", "b": "2" }}
                """;
        ObjectNode currentNode = asObjectNode(objectMapper.readTree(currentJson));
        ObjectNode currentNodeCopy = currentNode.deepCopy();
        var newJson = """
                {"c": "3"}
                """;
        ObjectNode newNode = asObjectNode(objectMapper.readTree(newJson));

        ObjectNode targetNode = asObjectNode(currentNode.path("propertyToUpdate"));
        targetNode.setAll(newNode);

        assertThat(currentNode).hasToString("""
                {"x":"100","propertyToUpdate":{"a":"1","b":"2","c":"3"}}""");
        System.out.println("before: " + currentNodeCopy);
        System.out.println("after: " + currentNode);
    }

    @ParameterizedTest
    @MethodSource("nameFieldCases")
    void nameFieldIsFetchedCorrectly(String json, String expected) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        String actual = root.path("name").asText("default");
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> nameFieldCases() {
        return Stream.of(
                argumentSet("existing field", """
                        {"name": "value"}
                        """, "value"),
                argumentSet("empty string field", """
                        {"name": ""}
                        """, ""),
                argumentSet("null field", """
                        {"name": null}
                        """, "default"),
                argumentSet("missing field", "{}", "default")
        );
    }

    @ParameterizedTest
    @MethodSource("nestedNameFieldCases")
    void nestedNameFieldIsFetchedCorrectly(String json, String expected) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        String actual = root.at(PERSON_NAME_FIELD_POINTER).asText("default");
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> nestedNameFieldCases() {
        return Stream.of(
                argumentSet("existing nested field", """
                        {
                          "person": {
                            "name": "value"
                          }
                        }
                        """, "value"),
                argumentSet("empty string field", """
                        {
                          "person": {
                            "name": ""
                          }
                        }
                        """, ""),
                argumentSet("null field", """
                        { "person": null }
                        """, "default"),
                argumentSet("nested null field", """
                        {
                          "person": {
                            "name": null
                          }
                        }
                        """, "default"),
                argumentSet("missing field", "{}", "default"),
                argumentSet("nested missing field", """
                        { "person": {} }
                        """, "default")
        );
    }

    private ObjectNode asObjectNode(JsonNode jsonNode) {
        if (jsonNode instanceof ObjectNode) {
            return (ObjectNode) jsonNode;
        }
        throw new IllegalArgumentException("Not an ObjectNode");
    }
}
