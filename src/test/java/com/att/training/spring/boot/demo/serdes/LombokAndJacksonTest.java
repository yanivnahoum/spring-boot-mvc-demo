package com.att.training.spring.boot.demo.serdes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;

class LombokAndJacksonTest {
    private final JsonMapper jsonMapper = new JsonMapper();

    @DisplayName("Most immutable pojos only require that the -parameters compiler arg is specified and that the " +
                 "ParameterNamesModule is added to the JsonMapper, which is done by default Jackson 3.")
    @Test
    void deserializeImmutablePojo() throws JacksonException {
        String json = singleToDoubleQuotes("{ 'x': '1', 'y': '2' }");
        var pojo = jsonMapper.readValue(json, ImmutablePojo.class);
        assertThat(pojo).usingRecursiveComparison()
                .isEqualTo(new ImmutablePojo(1, 2));
    }

    @DisplayName("""
            In Jackson3, immutable pojos with a single field created using lombok's @Value no longer need a special annotation. \
            Like they did in Jackson2 (lombok.config: lombok.anyConstructor.addConstructorProperties)
            """)
    @Test
    void deserializeImmutablePojoWithSingleField() throws JacksonException {
        String json = singleToDoubleQuotes("{ 'value': '10' }");
        var pojo = jsonMapper.readValue(json, ImmutablePojoWithSingleField.class);
        assertThat(pojo.getValue()).isEqualTo(10);
    }

    @Test
    void deserializeMutablePojo() throws JacksonException {
        String json = singleToDoubleQuotes("{ 'x': '1', 'y': '2' }");
        var pojo = jsonMapper.readValue(json, MutablePojo.class);
        assertThat(pojo.getX()).isEqualTo(1);
        assertThat(pojo.getY()).isEqualTo(2);
    }

    @Test
    void deserializeRecord() throws JacksonException {
        String json = singleToDoubleQuotes("{ 'x': '1', 'y': '2' }");
        var pojo = jsonMapper.readValue(json, SomeRecord.class);
        assertThat(pojo).isEqualTo(new SomeRecord(1, 2));
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

    @Data
    @AllArgsConstructor
    static class MutablePojo {
        private int x;
        private int y;
    }

    record SomeRecord(int x, int y) {}
}
