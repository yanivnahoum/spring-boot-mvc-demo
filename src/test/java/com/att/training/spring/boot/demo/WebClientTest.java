package com.att.training.spring.boot.demo;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
class WebClientTest {
    private MockWebServer mockWebServer;
    private WebClient webClient;

    @BeforeEach
    void init() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        var baseUrl = mockWebServer.url("/api").toString();
        webClient = WebClient.create(baseUrl);
    }

    @AfterEach
    void cleanup() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void requestShouldSucceed() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("""
                        { "id": "100", "name": "John" }
                        """)
        );

        var personMono = webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Person.class);

        StepVerifier.create(personMono)
                .assertNext(person -> assertThat(person).isNotNull()
                        .isEqualTo(new Person(100, "John")))
                .verifyComplete();
    }

    @Test
    void requestAsEntityShouldSucceed() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("""
                        { "id": "100", "name": "John" }
                        """)
        );

        var personResponseEntity = webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(Person.class)
                .block();

        assertThat(personResponseEntity).isNotNull()
                .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                .containsExactly(HttpStatus.OK, new Person(100, "John"));
    }


    @Test
    void givenEmptyResponse_pojoShouldBeNull() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        var person = webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Person.class)
                .block();

        assertThat(person).isNull();
    }

    @Test
    void givenEmptyResponse_entityShouldNotBeNull() {
        mockWebServer.enqueue(new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        var personResponseEntity = webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(Person.class)
                .block();

        assertThat(personResponseEntity).isNotNull()
                .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                .containsExactly(HttpStatus.OK, null);
        assertThat(personResponseEntity.hasBody()).isFalse();
    }

    @Test
    void given4xxResponse_pojoShouldBeNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(BAD_REQUEST.value())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        var personMono = webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Person.class)
                .onErrorResume(ex -> {
                    log.error("An error occurred", ex);
                    return Mono.empty();
                });

        StepVerifier.create(personMono)
                .verifyComplete();
    }

    @Test
    void given4xxResponse_pojoShouldBeNullAgain() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(BAD_REQUEST.value())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        var person = webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.empty())
                .bodyToMono(Person.class)
                .block();

        assertThat(person).isNull();
    }

    @Test
    void given4xxResponse_shouldThrowException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(BAD_REQUEST.value())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        var personResponseEntity = webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(Person.class);

        StepVerifier.create(personResponseEntity)
                .expectErrorMatches(WebClientResponseException.class::isInstance)
                .verify();
    }

    @Test
    void given4xxResponse_shouldReturn400() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(BAD_REQUEST.value())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

        var httpStatus = webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> Mono.just(response.statusCode()))
                .block();

        assertThat(httpStatus).isEqualTo(BAD_REQUEST);
    }
}

record Person(long id, String name) {}