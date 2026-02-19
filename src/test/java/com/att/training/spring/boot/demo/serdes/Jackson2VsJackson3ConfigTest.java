package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest
class Jackson2VsJackson3ConfigTest {
    @Autowired
    private JsonMapper jackson3Mapper;

    // Manually configure ObjectMapper with Jackson 2 defaults
    private final JsonMapper jackson2Mapper = JsonMapper.builder()
            .enable(MapperFeature.USE_GETTERS_AS_SETTERS)
            .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .build();

    @Nested
    class FailOnNullForPrimitives {

        record PrimitiveFields(int count, double value, boolean flag) {}

        @Test
        void jackson2_allowsNullForPrimitives() {
            new PrimitiveFields(0, 0d, false);
            var json = """
                    {
                        "count": null,
                        "value": null,
                        "flag": null
                    }
                    """;

            // Jackson 2 default: FAIL_ON_NULL_FOR_PRIMITIVES = false (coerces null to 0/false)
            var result = jackson2Mapper.readValue(json, PrimitiveFields.class);

            assertThat(result.count()).isZero();
            assertThat(result.value()).isZero();
            assertThat(result.flag()).isFalse();
        }

        @Test
        void jackson3_failsOnNullForPrimitives() {
            var json = """
                    {
                        "count": null,
                        "value": null,
                        "flag": null
                    }
                    """;

            // Jackson 3 default: FAIL_ON_NULL_FOR_PRIMITIVES = true
            assertThatThrownBy(() -> jackson3Mapper.readValue(json, PrimitiveFields.class))
                    .isInstanceOf(MismatchedInputException.class)
                    .hasMessageContaining("Cannot map `null` into type `int`");
        }
    }

    @Nested
    class UseGettersAsSetters {

        @Getter
        static class CollectionHolder {
            private final List<String> items = new ArrayList<>();
        }

        @Test
        void jackson2_usesGettersAsSetters() {
            var json = """
                    {
                        "items": ["apple", "banana"]
                    }
                    """;

            // Jackson 2 default: USE_GETTERS_AS_SETTERS = true
            // The getter is called and items are added to the existing collection
            CollectionHolder result = jackson2Mapper.readValue(json, CollectionHolder.class);

            // Note: In Spring Boot 4 / Jackson 3, even with USE_GETTERS_AS_SETTERS enabled,
            // the behavior might be different from Jackson 2.x
            assertThat(result.getItems())
                    .containsExactly("apple", "banana");
        }

        @Test
        void jackson3_doesNotUseGettersAsSetters() {
            var json = """
                    {
                        "items": ["apple", "banana"]
                    }
                    """;

            CollectionHolder result = jackson3Mapper.readValue(json, CollectionHolder.class);

            assertThat(result.getItems()).isEmpty();
        }

        @Getter
        @Setter
        static class MutableCollectionHolder {
            private List<String> items;
        }

        @Test
        void jackson3_doesNotUseGettersAsSettersAlternative1() {
            var json = """
                    {
                        "items": ["apple", "banana"]
                    }
                    """;

            MutableCollectionHolder result = jackson3Mapper.readValue(json, MutableCollectionHolder.class);

            assertThat(result.getItems()).containsExactly("apple", "banana");
        }

        record ImmutableCollectionHolder(List<String> items) {}

        @Test
        void jackson3_doesNotUseGettersAsSettersAlternative2() {
            var json = """
                    {
                        "items": ["apple", "banana"]
                    }
                    """;

            ImmutableCollectionHolder result = jackson3Mapper.readValue(json, ImmutableCollectionHolder.class);

            assertThat(result.items()).containsExactly("apple", "banana");
        }
    }

    @Nested
    class SortPropertiesAlphabetically {

        @Getter
        @Setter
        static class Person {
            private String name;
            private int age;
            private String country;
        }

        @Test
        void jackson2_doesNotSortProperties() {
            var person = new Person();
            person.setName("John");
            person.setAge(30);
            person.setCountry("USA");

            // Jackson 2 default: SORT_PROPERTIES_ALPHABETICALLY = false
            // Property order is based on declaration order (which may be unstable)
            var json = jackson2Mapper.writeValueAsString(person);

            // In Jackson 2, order follows declaration: name, age, city, country
            var expectedJson = """
                    {"name":"John","age":30,"country":"USA"}""";
            assertThat(json).isEqualTo(expectedJson);
        }

        @Test
        void jackson3_sortsPropertiesAlphabetically() {
            var person = new Person();
            person.setName("John");
            person.setAge(30);
            person.setCountry("USA");

            // Jackson 3 default: SORT_PROPERTIES_ALPHABETICALLY = true (per documentation)
            // However, Spring Boot's auto-configuration may override this
            var json = jackson3Mapper.writeValueAsString(person);

            // In this Spring Boot 4 setup, properties follow declaration order
            var expectedJson = """
                    {"age":30,"country":"USA","name":"John"}""";
            assertThat(json).isEqualTo(expectedJson);
        }
    }

    @Nested
    class AllowFinalFieldsAsMutators {

        @Getter
        static class FinalFieldContainer {
            private final String name;

            public FinalFieldContainer() {
                this.name = "default";
            }
        }

        @Test
        void jackson2_allowsFinalFieldsAsMutators() {
            var json = """
                    {
                        "name": "Alice"
                    }
                    """;

            // Jackson 2 default: ALLOW_FINAL_FIELDS_AS_MUTATORS = true
            // Final fields can be mutated through their getters
            var result = jackson2Mapper.readValue(json, FinalFieldContainer.class);

            assertThat(result.getName()).isEqualTo("Alice");
        }

        @Test
        void jackson3_doesNotAllowFinalFieldsAsMutators() {
            var json = """
                    {
                        "name": "Alice"
                    }
                    """;

            // Jackson 3 default: ALLOW_FINAL_FIELDS_AS_MUTATORS = false (per documentation)
            // Final fields without setters are ignored
            var result = jackson3Mapper.readValue(json, FinalFieldContainer.class);

            // The final field is not populated - the items list remains empty
            assertThat(result.getName()).isEqualTo("default");
        }
    }

    @Nested
    class EnumToStringHandling {

        enum Status {
            ACTIVE,
            INACTIVE;

            @Override
            public String toString() {
                return name().toLowerCase();
            }
        }

        record StatusHolder(Status status) {}

        @Test
        void jackson2_usesEnumName() {
            var holder = new StatusHolder(Status.ACTIVE);

            // Jackson 2 default: WRITE_ENUMS_USING_TO_STRING = false
            // Uses Enum.name() for serialization
            var json = jackson2Mapper.writeValueAsString(holder);

            assertThat(json).isEqualTo("""
                    {"status":"ACTIVE"}\
                    """);

            // Jackson 2 default: READ_ENUMS_USING_TO_STRING = false
            // Uses enum.name() matching for deserialization
            var result = jackson2Mapper.readValue("""
                    {"status":"INACTIVE"}\
                    """, StatusHolder.class);

            assertThat(result.status()).isEqualTo(Status.INACTIVE);
        }

        @Test
        void jackson3_usesToString() {
            var holder = new StatusHolder(Status.ACTIVE);

            // Jackson 3 default: WRITE_ENUMS_USING_TO_STRING = true
            // Uses Enum.toString() for serialization
            var json = jackson3Mapper.writeValueAsString(holder);

            assertThat(json).isEqualTo("""
                    {"status":"active"}\
                    """);

            // Jackson 3 default: READ_ENUMS_USING_TO_STRING = true
            // Uses toString() matching for deserialization
            var result = jackson3Mapper.readValue("""
                    {"status":"inactive"}\
                    """, StatusHolder.class);

            assertThat(result.status()).isEqualTo(Status.INACTIVE);
        }

        @Test
        void jackson3_stillUsesJsonValue() {
            @RequiredArgsConstructor
            @Getter
            enum COLOR {
                RED("rosso"),
                GREEN("verde"),
                BLUE("blu");

                @JsonValue
                private final String value;
            }
            record ColorHolder(COLOR color) {}
            var holder = new ColorHolder(COLOR.RED);

            var json = jackson3Mapper.writeValueAsString(holder);

            assertThat(json).isEqualTo("""
                    {"color":"rosso"}\
                    """);

            var result = jackson3Mapper.readValue("""
                    {"color":"verde"}\
                    """, ColorHolder.class);

            assertThat(result.color()).isEqualTo(COLOR.GREEN);
        }
    }
}
