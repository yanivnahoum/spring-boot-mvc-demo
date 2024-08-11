package com.att.training.spring.boot.demo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.util.ArrayList;
import java.util.List;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonTest {

    @Autowired private ObjectMapper mapper;

    @Nested
    @DisplayName("When deserializing to a SimpleDto")
    class SimpleDtoLists {

        @Test
        void givenJsonListWithValues_shouldDeserializeToListWithValues() throws JsonProcessingException {
            String json = singleToDoubleQuotes("{'values': ['a','b','c']}");

            SomeDto deserializedDto = mapper.readValue(json, SomeDto.class);

            assertThat(deserializedDto.getValues()).containsExactly("a", "b", "c");
        }

        @Test
        void givenNullJsonList_shouldDeserializeToNullList() throws JsonProcessingException {
            String json = singleToDoubleQuotes("{'values': null}");

            SomeDto deserializedDto = mapper.readValue(json, SomeDto.class);

            assertThat(deserializedDto.getValues()).isNull();
        }

        @Test
        void givenEmptyJsonList_shouldDeserializeToEmptyList() throws JsonProcessingException {
            String json = singleToDoubleQuotes("{'values': []}");

            SomeDto deserializedDto = mapper.readValue(json, SomeDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }

        @Test
        void givenNullJsonList_shouldDeserializeToEmptyList() throws JsonProcessingException {
            var modifiedMapper = mapper.copy();
            modifiedMapper.configOverride(List.class)
                    .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

            String json = singleToDoubleQuotes("{'values': null}");
            SomeDto deserializedDto = modifiedMapper.readValue(json, SomeDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }

        @Test
        void givenMissingJsonList_shouldDeserializeToEmptyList() throws JsonProcessingException {
            var modifiedMapper = mapper.copy();
            modifiedMapper.configOverride(List.class)
                    .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

            SomeDto deserializedDto = modifiedMapper.readValue("{}", SomeDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }
    }

    @Nested
    @DisplayName("When deserializing to a FinalDto")
    class FinalDtoLists {

        @Test
        void givenJsonListWithValues_shouldDeserializeToListWithValues() throws JsonProcessingException {
            String json = singleToDoubleQuotes("{'values': ['a','b','c']}");

            FinalDto deserializedDto = mapper.readValue(json, FinalDto.class);

            assertThat(deserializedDto.getValues()).containsExactly("a", "b", "c");
        }

        @Test
        void givenNullJsonList_shouldDeserializeToNullList() throws JsonProcessingException {
            String json = singleToDoubleQuotes("{'values': null}");

            FinalDto deserializedDto = mapper.readValue(json, FinalDto.class);

            assertThat(deserializedDto.getValues()).isNull();
        }

        @Test
        void givenEmptyJsonList_shouldDeserializeToEmptyList() throws JsonProcessingException {
            String json = singleToDoubleQuotes("{'values': []}");

            FinalDto deserializedDto = mapper.readValue(json, FinalDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }

        @Test
        void givenNullJsonList_shouldDeserializeToEmptyList() throws JsonProcessingException {
            var modifiedMapper = mapper.copy();
            modifiedMapper.configOverride(List.class)
                    .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));

            String json = singleToDoubleQuotes("{'values': null}");
            FinalDto deserializedDto = modifiedMapper.readValue(json, FinalDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }

        @Test
        void givenMissingJsonList_shouldDeserializeToEmptyList() throws JsonProcessingException {
            var modifiedMapper = mapper.copy();
            modifiedMapper.configOverride(List.class)
                    .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));

            FinalDto deserializedDto = modifiedMapper.readValue("{}", FinalDto.class);

            assertThat(deserializedDto.getValues()).isEmpty();
        }
    }

    @Value
    static class SomeDto {
        List<String> values;
    }

    @Getter
    static class FinalDto {
        private final List<String> values = new ArrayList<>();
    }

}



