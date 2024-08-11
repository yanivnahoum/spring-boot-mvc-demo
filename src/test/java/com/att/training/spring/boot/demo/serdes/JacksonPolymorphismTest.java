package com.att.training.spring.boot.demo.serdes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JacksonPolymorphismTest {
    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Nested
    class ClassTest {
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
                    "@class": "com.att.training.spring.boot.demo.serdes.JacksonPolymorphismTest$ClassTest$M1",
                     "m1": "I'm m1"
                }
                """;
            var m2Json = """
                {
                    "@class": "com.att.training.spring.boot.demo.serdes.JacksonPolymorphismTest$ClassTest$M2",
                     "m2": "I'm m2"
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
