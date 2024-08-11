package com.att.training.spring.boot.demo.quote;

import com.att.training.spring.boot.demo.quote.api.Contents;
import com.att.training.spring.boot.demo.quote.api.Copyright;
import com.att.training.spring.boot.demo.quote.api.QuoteDetails;
import com.att.training.spring.boot.demo.quote.api.QuoteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class BasicWireMockQuoteClientTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final WireMockServer wireMockServer = createStarted();
    private QuoteClient quoteClient;

    @BeforeEach
    void before() {
        var quoteClientProperties = new QuoteClientProperties(wireMockServer.baseUrl(), Duration.ofSeconds(1), Duration.ofSeconds(1));
        quoteClient = new QuoteClient(new RestTemplateBuilder(), new QuoteClientResponseErrorHandler(objectMapper), quoteClientProperties);
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void after() {
        wireMockServer.stop();
    }

    @Test
    void givenCategoryFunny_whenGetQuoteOfTheDay_thenCorrectQuoteIsReceived() {
        var category = "funny";
        wireMockServer.givenThat(get(urlPathEqualTo("/qod"))
                .withQueryParam("category", equalTo(category))
                // withBodyFile() looks under src/test/resources/__files by default
                .willReturn(ok().withBodyFile("funny-qod.json")
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        QuoteResponse funnyQuote = quoteClient.getQuoteOfTheDay(category);

        QuoteResponse expectedQuote = new QuoteResponse(
                new Contents(List.of(new QuoteDetails("Give me golf clubs, fresh air and a beautiful partner, and you can keep the clubs and the fresh air.", "Jack Benny"))),
                new Copyright(2022, "https://theysaidso.com"));
        assertThat(funnyQuote).isEqualTo(expectedQuote);
    }

    private static WireMockServer createStarted() {
        WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        return wireMockServer;
    }
}
