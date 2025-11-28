package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonDeserializingListTest {
    @Autowired
    private JsonMapper mapper;

    @Nested
    @DisplayName("When deserializing to a record DTO")
    class RecordDtoLists {

        @Test
        void givenJsonListWithValues_shouldDeserializeToListWithValues() throws JacksonException {
            String json = singleToDoubleQuotes("{'values': ['a','b','c']}");

            SomeDto deserializedDto = mapper.readValue(json, SomeDto.class);

            assertThat(deserializedDto.values()).containsExactly("a", "b", "c");
        }

        @Test
        void givenNullJsonList_shouldDeserializeToNullList() throws JacksonException {
            String json = singleToDoubleQuotes("{'values': null}");

            SomeDto deserializedDto = mapper.readValue(json, SomeDto.class);

            assertThat(deserializedDto.values()).isNull();
        }

        @Test
        void givenEmptyJsonList_shouldDeserializeToEmptyList() throws JacksonException {
            String json = singleToDoubleQuotes("{'values': []}");

            SomeDto deserializedDto = mapper.readValue(json, SomeDto.class);

            assertThat(deserializedDto.values()).isEmpty();
        }

        @Test
        void givenNullJsonList_shouldDeserializeToEmptyList() throws JacksonException {
            JsonMapper modifiedMapper = mapper.rebuild()
                    .withConfigOverride(List.class, config ->
                            config.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY)))
                    .build();

            String json = singleToDoubleQuotes("{'values': null}");
            SomeDto deserializedDto = modifiedMapper.readValue(json, SomeDto.class);

            assertThat(deserializedDto.values()).isEmpty();
        }

        @Test
        void givenMissingJsonList_shouldDeserializeToEmptyList() throws JacksonException {
            JsonMapper modifiedMapper = mapper.rebuild()
                    .withConfigOverride(List.class, config ->
                            config.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY)))
                    .build();

            SomeDto deserializedDto = modifiedMapper.readValue("{}", SomeDto.class);

            assertThat(deserializedDto.values()).isEmpty();
        }
    }

    @Nested
    @DisplayName("When deserializing to a MutableDto")
    class MutableDtoLists {

        @Test
        void givenJsonListWithValues_shouldDeserializeToListWithValues() throws JacksonException {
            String json = singleToDoubleQuotes("{'values': ['a','b','c']}");

            MutableDto deserializedDto = mapper.readValue(json, MutableDto.class);

            assertThat(deserializedDto.getValues()).containsExactly("a", "b", "c");
        }

        @Test
        void givenNullJsonList_shouldDeserializeToNullList() throws JacksonException {
            String json = singleToDoubleQuotes("{'values': null}");

            MutableDto deserializedDto = mapper.readValue(json, MutableDto.class);

            assertThat(deserializedDto.getValues()).isNull();
        }

        @Test
        void givenEmptyJsonList_shouldDeserializeToEmptyList() throws JacksonException {
            String json = singleToDoubleQuotes("{'values': []}");

            MutableDto deserializedDto = mapper.readValue(json, MutableDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }

        @Test
        void givenNullJsonList_shouldDeserializeToEmptyList() throws JacksonException {
            JsonMapper modifiedMapper = mapper.rebuild()
                    .withConfigOverride(List.class, config ->
                            config.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.SKIP)))
                    .build();

            String json = singleToDoubleQuotes("{'values': null}");
            MutableDto deserializedDto = modifiedMapper.readValue(json, MutableDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }

        @Test
        void givenMissingJsonList_shouldDeserializeToEmptyList() throws JacksonException {
            JsonMapper modifiedMapper = mapper.rebuild()
                    .withConfigOverride(List.class, config ->
                            config.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.SKIP)))
                    .build();

            MutableDto deserializedDto = modifiedMapper.readValue("{}", MutableDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }
    }

    record SomeDto(List<String> values) {}

    @Getter
    @Setter
    static class MutableDto {
        private List<String> values = new ArrayList<>();
    }
}
