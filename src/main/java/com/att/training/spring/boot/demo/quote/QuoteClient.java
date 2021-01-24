package com.att.training.spring.boot.demo.quote;

import com.att.training.spring.boot.demo.quote.api.QuoteResponse;
import okhttp3.OkHttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static java.net.Proxy.Type.HTTP;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class QuoteClient {

    private final RestTemplate restTemplate;

    public QuoteClient(RestTemplateBuilder builder, QuoteClientResponseErrorHandler errorHandler) {
        this.restTemplate = builder
                .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(newClient()))
                .rootUri("http://quotes.rest")
                .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
                .errorHandler(errorHandler)
                .build();
    }

    public QuoteResponse getQuoteOfTheDay(String category) {
        return restTemplate.getForObject("/qod?category={category}", QuoteResponse.class, category);
    }

    private OkHttpClient newClient() {
        return new OkHttpClient.Builder()
                .proxy(new Proxy(HTTP, new InetSocketAddress("emea-chain.proxy.att.com", 8080)))
                .build();
    }
}
