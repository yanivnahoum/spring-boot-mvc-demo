package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.beans.ConstructorProperties;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

class JacksonPolymorphismTest {
    // In Spring Boot, the auto-configured ObjectMapper is similar to the one below:
    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addModule(new ParameterNamesModule())
            .build();

    @Nested
    class ClassTestWithRecords {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        interface Message {}

        record M1(String m1) implements Message {}

        record M2(String m2) implements Message {}

        @Test
        void write() throws JsonProcessingException {
            var m1 = new M1("I'm m1");
            var m2 = new M2("I'm m2");
            System.out.println(mapper.writeValueAsString(m1));
            System.out.println(mapper.writeValueAsString(m2));
        }

        @Test
        void read() throws JsonProcessingException {
            var m1Json = """
                    {
                        "@class": "com.att.training.spring.boot.demo.serdes.JacksonPolymorphismTest$ClassTestWithRecords$M1",
                         "m1": "I'm m1"
                    }
                    """;
            var m2Json = """
                    {
                        "@class": "com.att.training.spring.boot.demo.serdes.JacksonPolymorphismTest$ClassTestWithRecords$M2",
                         "m2": "I'm m2"
                    }
                    """;
            System.out.println(mapper.readValue(m1Json, Message.class));
            System.out.println(mapper.readValue(m2Json, Message.class));
        }
    }

    @Nested
    class ClassTest {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        @RequiredArgsConstructor
        @Getter
        @ToString
        static abstract class Message {
            private final String message;
        }

        @ToString(callSuper = true)
        static class M1 extends Message {
            // We have an issue with single property c'tors.
            // Here's one way to solve it.
            // We configured lombok to add the @ConstructorProperties annotation, but since we
            // have a base class, we can't use lombok to generate the c'tor.
            @ConstructorProperties("message")
            public M1(String message) {
                super(message);
            }
        }

        @ToString(callSuper = true)
        static class M2 extends Message {
            // Here's another way to solve the single property c'tor issue.
            // It assumes we registered the ParameterNamesModule (as in Spring Boot's auto-configured ObjectMapper):
            @JsonCreator(mode = PROPERTIES)
            public M2(String message) {
                super(message);
            }
        }

        @Test
        void write() throws JsonProcessingException {
            var m1 = new M1("I'm m1");
            var m2 = new M2("I'm m2");
            System.out.println(mapper.writeValueAsString(m1));
            System.out.println(mapper.writeValueAsString(m2));
        }

        @Test
        void read() throws JsonProcessingException {
            var m1Json = """
                    {
                        "@class": "com.att.training.spring.boot.demo.serdes.JacksonPolymorphismTest$ClassTest$M1",
                         "message": "I'm m1"
                    }
                    """;
            var m2Json = """
                    {
                        "@class": "com.att.training.spring.boot.demo.serdes.JacksonPolymorphismTest$ClassTest$M2",
                         "message": "I'm m2"
                    }
                    """;
            System.out.println(mapper.readValue(m1Json, Message.class));
            System.out.println(mapper.readValue(m2Json, Message.class));
        }
    }

    @Nested
    class NameTest {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
        @JsonSubTypes({
                @JsonSubTypes.Type(value = N1.class, name = "one"),
                @JsonSubTypes.Type(N2.class),
        })
        interface Notification {}

        record N1(String n1) implements Notification {}

        @JsonTypeName("two")
        record N2(String n2) implements Notification {}

        @Test
        void write() throws JsonProcessingException {
            var n1 = new N1("I'm n1");
            var n2 = new N2("I'm n2");
            System.out.println(mapper.writeValueAsString(n1));
            System.out.println(mapper.writeValueAsString(n2));
        }

        @Test
        void read() throws JsonProcessingException {
            var n1Json = """
                    { "@type": "one", "n1": "I'm n1" }
                    """;
            var n2Json = """
                    { "@type": "two", "n2": "I'm n2" }
                    """;
            System.out.println(mapper.readValue(n1Json, Notification.class));
            System.out.println(mapper.readValue(n2Json, Notification.class));
        }
    }
}
