package com.att.training.spring.boot.demo.quote;

import com.att.training.spring.boot.demo.quote.api.Contents;
import com.att.training.spring.boot.demo.quote.api.Copyright;
import com.att.training.spring.boot.demo.quote.api.QuoteDetails;
import com.att.training.spring.boot.demo.quote.api.QuoteError;
import com.att.training.spring.boot.demo.quote.api.QuoteResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest({QuoteClient.class, QuoteClientResponseErrorHandler.class})
class QuoteClientTest {

    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private QuoteClient quoteClient;

    @Test
    void givenCategoryFunny_whenGetQuoteOfTheDay_thenCorrectQuoteIsReceived() {
        String category = "funny";
        server.expect(once(), requestTo(startsWith("/qod")))
                .andExpect(method(GET))
                .andExpect(queryParam("category", category))
                .andRespond(withSuccess(new ClassPathResource("funny-qod.json", getClass()), APPLICATION_JSON));

        QuoteResponse funnyQuote = quoteClient.getQuoteOfTheDay(category);

        QuoteResponse expectedQuote = new QuoteResponse(
                new Contents(List.of(new QuoteDetails("Give me golf clubs, fresh air and a beautiful partner, and you can keep the clubs and the fresh air.", "Jack Benny"))),
                new Copyright(2022, "https://theysaidso.com"));
        assertThat(funnyQuote).isEqualTo(expectedQuote);
    }

    @Test
    void givenMissingCategory_whenGetQuoteOfTheDay_thenCorrectQuoteErrorIsReceived() {
        String missingCategory = "blabla";
        server.expect(requestTo(startsWith("/qod")))
                .andExpect(method(GET))
                .andExpect(queryParam("category", missingCategory))
                .andRespond(withBadRequest()
                        .body(new ClassPathResource("no-category.json", getClass()))
                        .contentType(APPLICATION_JSON));

        QuoteError expectedError = new QuoteError(400, "Bad Request: QOD category not supported for this category and language combination");
        assertThatExceptionOfType(QuoteClientException.class)
                .isThrownBy(() -> quoteClient.getQuoteOfTheDay(missingCategory))
                .matches(ex -> expectedError.equals(ex.getQuoteError()));
    }
}