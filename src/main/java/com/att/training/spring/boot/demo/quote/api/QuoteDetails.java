package com.att.training.spring.boot.demo.quote.api;

import lombok.Value;

@Value
public class QuoteDetails {
    String quote;
    String author;
}
