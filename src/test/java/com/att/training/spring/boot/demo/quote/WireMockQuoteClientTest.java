package com.att.training.spring.boot.demo.quote;

import com.att.training.spring.boot.demo.quote.api.Contents;
import com.att.training.spring.boot.demo.quote.api.Copyright;
import com.att.training.spring.boot.demo.quote.api.QuoteDetails;
import com.att.training.spring.boot.demo.quote.api.QuoteResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@AutoConfigureWebClient
@AutoConfigureWireMock(port = 0)
@Import({QuoteClient.class, QuoteClientResponseErrorHandler.class})
class WireMockQuoteClientTest {

    @Autowired private QuoteClient quoteClient;

    @Test
    void givenCategoryFunny_whenGetQuoteOfTheDay_thenCorrectQuoteIsReceived() {
        QuoteResponse funnyQuote = quoteClient.getQuoteOfTheDay("funny");

        QuoteResponse expectedQuote = new QuoteResponse(
                new Contents(List.of(new QuoteDetails("Give me golf clubs, fresh air and a beautiful partner, and you can keep the clubs and the fresh air.", "Jack Benny"))),
                new Copyright(2022, "https://theysaidso.com"));
        assertThat(funnyQuote).isEqualTo(expectedQuote);
    }

    private static String asString(Resource resource) {
        try (var reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class AdditionalConfig {

        @Bean
        // Allow WireMockServer to be created and started
        @Lazy
        RestTemplateCustomizer customizer(WireMockServer server) {
            return restTemplate -> {
                RootUriTemplateHandler.addTo(restTemplate, server.baseUrl());
                // Don't register proxy
                restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory());
            };
        }
    }
}
