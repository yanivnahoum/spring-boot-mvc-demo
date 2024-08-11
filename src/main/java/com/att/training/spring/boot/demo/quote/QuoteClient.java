package com.att.training.spring.boot.demo.quote;

import com.att.training.spring.boot.demo.quote.api.QuoteResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class QuoteClient {
    private final RestTemplate restTemplate;

    public QuoteClient(RestTemplateBuilder builder, QuoteClientResponseErrorHandler errorHandler, QuoteClientProperties properties) {
        this.restTemplate = builder
                .rootUri(properties.baseUrl())
                .setConnectTimeout(properties.connectTimeout())
                .setReadTimeout(properties.readTimeout())
                .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
                .errorHandler(errorHandler)
                .build();
    }

    public QuoteResponse getQuoteOfTheDay(String category) {
        return restTemplate.getForObject("/qod?category={category}", QuoteResponse.class, category);
    }
}
