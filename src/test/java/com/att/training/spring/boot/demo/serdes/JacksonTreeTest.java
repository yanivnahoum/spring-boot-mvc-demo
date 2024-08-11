package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.Test;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;

class JacksonTreeTest {
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new ParameterNamesModule())
            .build();

    @Test
    void putAllProperties() throws JsonProcessingException {
        var currentJson = singleToDoubleQuotes("{ 'x': '100', 'propertyToUpdate': { 'a': '1', 'b': '2' } }");
        ObjectNode currentNode = asObjectNode(objectMapper.readTree(currentJson));
        ObjectNode currentNodeCopy = currentNode.deepCopy();
        var newJson = singleToDoubleQuotes("{ 'c': '3' }");
        ObjectNode newNode = asObjectNode(objectMapper.readTree(newJson));

        ObjectNode targetNode = asObjectNode(currentNode.findPath("propertyToUpdate"));
        targetNode.setAll(newNode);

        assertThat(currentNode).hasToString(singleToDoubleQuotes("{'x':'100','propertyToUpdate':{'a':'1','b':'2','c':'3'}}"));
        System.out.println("before: " + currentNodeCopy);
        System.out.println("after: " + currentNode);
    }

    private ObjectNode asObjectNode(JsonNode jsonNode) {
        if (jsonNode instanceof ObjectNode) {
            return (ObjectNode) jsonNode;
        }
        throw new IllegalArgumentException("Not an ObjectNode");
    }
}
