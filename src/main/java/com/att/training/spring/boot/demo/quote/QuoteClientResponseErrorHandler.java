package com.att.training.spring.boot.demo.quote;

import com.att.training.spring.boot.demo.quote.api.QuoteError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

import static com.google.common.base.MoreObjects.firstNonNull;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuoteClientResponseErrorHandler extends DefaultResponseErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    protected void handleError(@NonNull ClientHttpResponse response, @NonNull HttpStatusCode statusCode) throws IOException {
        try {
            super.handleError(response, statusCode);
        } catch (RestClientResponseException e) {
            QuoteError quoteError;
            try {
                quoteError = objectMapper.readValue(e.getResponseBodyAsString(), QuoteError.class);
            } catch (IOException ioEx) {
                log.warn("#handleError - an error occurred: ", ioEx);
                quoteError = new QuoteError(e.getStatusCode().value(), "N/A");
            }
            throw new QuoteClientException(e, quoteError);
        }
    }
}

@Getter
class QuoteClientException extends RestClientResponseException {

    private final QuoteError quoteError;

    public QuoteClientException(RestClientResponseException e, QuoteError quoteError) {
        super(firstNonNull(e.getMessage(), "N/A"), e.getStatusCode().value(), e.getStatusText(), e.getResponseHeaders(), e.getResponseBodyAsByteArray(), null);
        this.quoteError = quoteError;
    }
}