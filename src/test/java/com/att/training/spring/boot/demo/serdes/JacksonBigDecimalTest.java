package com.att.training.spring.boot.demo.serdes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.context.annotation.Bean;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static com.att.training.spring.boot.demo.utils.JsonUtils.singleToDoubleQuotes;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class JacksonBigDecimalTest {
    @TestConfiguration
    static class TestConfig {
        @Bean
        JsonMapperBuilderCustomizer customizer() {
            return builder -> builder
//                    .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                    .enable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES);
//                            .configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false)
//                            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
//                                    .configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(STRING))
        }
    }

    private final BasicJsonTester json = new BasicJsonTester(getClass());
    @Autowired
    private JsonMapper mapper;

    @Test
    void givenBigDecimalWithTrailingZero_whenReadValue_thenTrailingZeroIsKept() throws JacksonException {
        String json = singleToDoubleQuotes("{ 'value': 2.00 }");
        var bigDecimalHolder = mapper.readValue(json, BigDecimalHolder.class);
        assertThat(bigDecimalHolder.value().toPlainString()).isEqualTo("2.00");
    }

    @Test
    void givenJsonNodeWithBigDecimalWithTrailingZero_whenTreeToValue_thenTrailingZeroIsKept() throws JacksonException {
        var value = new BigDecimal("2.00");
        var jsonNode = mapper.createObjectNode()
                .put("value", value);
        var bigDecimalHolder = mapper.treeToValue(jsonNode, BigDecimalHolder.class);
        assertThat(bigDecimalHolder.value().toPlainString()).isEqualTo("2.00");
    }

    @Test
    void givenBigDecimalWithTrailingZero_whenWriteValue_thenTrailingZeroIsKept() throws JacksonException {
        var bigDecimalHolder = new BigDecimalHolder(new BigDecimal("2.00"));
        String actualJson = mapper.writeValueAsString(bigDecimalHolder);
        String expectedJson = singleToDoubleQuotes("{ 'value': 2.00 }");
        assertThat(json.from(actualJson)).isEqualToJson(expectedJson);
    }

    @Test
    void givenBigDecimalWithTrailingZero_whenValueToTree_thenTrailingZeroIsKept() {
        var bigDecimalHolder = new BigDecimalHolder(new BigDecimal("2.00"));
        JsonNode jsonNode = mapper.valueToTree(bigDecimalHolder);
        assertThat(jsonNode.get("value").asString()).isEqualTo("2.00");
    }


    record BigDecimalHolder(/*@JsonFormat(shape = STRING)*/BigDecimal value) {
    }
}
