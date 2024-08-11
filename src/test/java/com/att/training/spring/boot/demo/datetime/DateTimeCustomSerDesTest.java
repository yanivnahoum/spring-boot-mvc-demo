package com.att.training.spring.boot.demo.datetime;

import com.att.training.spring.boot.demo.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DateTimeCustomSerDesTest.TestConfig.class)
class DateTimeCustomSerDesTest {

    private static final String JSON = "{ 'instant': '2020-12-31T23:59:59Z', 'localDate': '2020-12-31', 'localDateTime': '2020-12-31T23:59:59Z' }";
    @Autowired private MockMvc mockMvc;

    @Test
    void putShouldReturn200Ok() throws Exception {
        mockMvc.perform(put("/utc")
                .contentType(APPLICATION_JSON)
                .content(JsonUtils.singleToDoubleQuotes(JSON)))
                .andExpect(status().isOk());
    }

    @Test
    void getShouldReturnLocalDateTimeInUTC() throws Exception {
        mockMvc.perform(get("/utc"))
                .andExpect(content().json(JSON))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        Jackson2ObjectMapperBuilderCustomizer customizer() {
            return builder -> {
                builder.serializers(new LocalDateTimeToUtcSerializer());
                builder.deserializers(new LocalDateTimeFromUtcDeserializer());
            };
        }
    }
}

@RestController
@RequestMapping("utc")
@Slf4j
class UtcDateTimeController {

    @PutMapping
    void create(@RequestBody SomePojo somePojo) {
        log.info("#create - {}", somePojo);
    }

    @GetMapping
    SomePojo get() {
        return new SomePojo(Instant.parse("2020-12-31T23:59:59Z"),
                LocalDate.of(2020, 12, 31),
                // This assumes we're running in TZ Asia/Jerusalem
                LocalDateTime.parse("2021-01-01T01:59:59"));
    }
}

@TestComponent
@Value
class SomePojo {
    Instant instant;
    LocalDate localDate;
    LocalDateTime localDateTime;
}

class LocalDateTimeToUtcSerializer extends StdSerializer<LocalDateTime> {

    LocalDateTimeToUtcSerializer() {
        this(LocalDateTime.class);
    }

    protected LocalDateTimeToUtcSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Instant instant = value.truncatedTo(ChronoUnit.SECONDS)
                .atZone(ZoneOffset.systemDefault())
                .toInstant();
        gen.writeString(instant.toString());
    }
}

class LocalDateTimeFromUtcDeserializer extends LocalDateTimeDeserializer {

    LocalDateTimeFromUtcDeserializer() {
        super(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        var ldt = super.deserialize(jsonParser, deserializationContext);
        return ldt.truncatedTo(ChronoUnit.SECONDS)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneOffset.systemDefault())
                .toLocalDateTime();
    }
}
