package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.Data;
import lombok.Value;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.beans.ConstructorProperties;
import java.util.stream.Stream;

import static com.att.training.spring.boot.demo.serdes.JacksonDeserializationOfEnumsWithoutNullsTest.Status.NONE;
import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static java.util.Objects.requireNonNullElse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@JsonTest
class JacksonDeserializationOfEnumsWithoutNullsTest {
    @TestConfiguration
    static class TestConfig {
        @Bean
        Jackson2ObjectMapperBuilderCustomizer customizer() {
            return builder -> builder.postConfigurer(mapper ->
                    // Needed for mutable POJOs. Can also be done via empty c'tor like in records.
                    mapper.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(SKIP)));

        }
    }

    @Autowired
    private ObjectMapper mapper;

    @ParameterizedTest
    @ValueSource(classes = {SomeMutablePojo.class, SomeImmutablePojo.class})
    void knownEnumValueIsDeserializedCorrectly(Class<? extends StatusProvider> klass) throws JsonProcessingException {
        var somePojo = mapper.readValue(singleToDoubleQuotes("{ 'status': 'ACTIVE' }"), klass);
        assertThat(somePojo.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @ParameterizedTest
    @MethodSource("classSource")
    void unknownEnumValueThrowsException(Class<? extends StatusProvider> klass) {
        assertThatExceptionOfType(InvalidFormatException.class).isThrownBy(() ->
                mapper.readValue(singleToDoubleQuotes("{ 'status': 'BLA' }"), klass));
    }

    @ParameterizedTest
    @MethodSource("classSource")
    void nullEnumValueIsDeserializedCorrectly(Class<? extends StatusProvider> klass) throws JsonProcessingException {
        var somePojo = mapper.readValue(singleToDoubleQuotes("{ 'status': null }"), klass);
        assertThat(somePojo.getStatus()).isEqualTo(NONE);
    }

    @ParameterizedTest
    @MethodSource("classSource")
    void missingEnumValueIsDeserializedCorrectly(Class<? extends StatusProvider> klass) throws JsonProcessingException {
        var somePojo = mapper.readValue("{}", klass);
        assertThat(somePojo.getStatus()).isEqualTo(NONE);
    }

    static Stream<Arguments> classSource() {
        return Stream.of(
                arguments(named(SomeMutablePojo.class.getSimpleName(), SomeMutablePojo.class)),
                arguments(named(SomeImmutablePojo.class.getSimpleName(), SomeImmutablePojo.class)),
                arguments(named(SomeRecord.class.getSimpleName(), SomeRecord.class))
        );
    }

    interface StatusProvider {
        Status getStatus();
    }

    @Data
    static class SomeMutablePojo implements StatusProvider {
        // Skip nulls on specific field:
        // @JsonSetter(nulls = SKIP)
        private Status status = NONE;

        // Another option:
//        public Status getStatus() {
//            return requireNonNullElse(status, NONE);
//        }
    }

    // This class is mostly for pre-java 17 code.
    @Value
    static class SomeImmutablePojo implements StatusProvider {
        Status status;

        // Required by Jackson for deserialization of single param constructors.
        // We usually provide this with Lombok (when we let it generate the constructor)
        @ConstructorProperties("status")
        public SomeImmutablePojo(Status status) {
            this.status = requireNonNullElse(status, NONE);
        }
    }

    record SomeRecord(Status status) implements StatusProvider {
        SomeRecord {
            status = requireNonNullElse(status, NONE);
        }


        @Override
        public Status getStatus() {
            return status;
        }
    }

    enum Status {
        ACTIVE,
        INACTIVE,
        NONE,
    }
}
