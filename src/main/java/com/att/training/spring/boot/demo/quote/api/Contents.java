package com.att.training.spring.boot.demo.quote.api;

import lombok.Value;

import java.util.List;

@Value
public class Contents {
    List<QuoteDetails> quotes;
}
