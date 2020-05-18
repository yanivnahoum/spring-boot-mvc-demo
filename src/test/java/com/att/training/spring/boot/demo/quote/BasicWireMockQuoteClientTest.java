package com.att.training.spring.boot.demo.quote;

import com.att.training.spring.boot.demo.quote.api.Contents;
import com.att.training.spring.boot.demo.quote.api.Copyright;
import com.att.training.spring.boot.demo.quote.api.QuoteDetails;
import com.att.training.spring.boot.demo.quote.api.QuoteResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebClient
@Import({QuoteClient.class, QuoteClientResponseErrorHandler.class})
class BasicWireMockQuoteClientTest {

    private static final WireMockServer wireMockServer = createStarted();
    @Autowired private QuoteClient quoteClient;

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

    @NotNull
    private static WireMockServer createStarted() {
        WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());
        // Start very early so that the baseUrl() could be fetched in the AdditionalConfig#customizer
        wireMockServer.start();
        return wireMockServer;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class AdditionalConfig {

        @Bean
        RestTemplateCustomizer customizer() {
            return restTemplate -> {
                RootUriTemplateHandler.addTo(restTemplate, wireMockServer.baseUrl());
                // Don't register proxy
                restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory());
            };
        }
    }
}
