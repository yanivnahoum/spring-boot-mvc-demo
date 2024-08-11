package com.att.training.spring.boot.demo.quote;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.quote-client")
@Validated
public record QuoteClientProperties(
        @NotEmpty String baseUrl,
        @NotNull @DefaultValue("10s") Duration connectTimeout,
        @NotNull @DefaultValue("30s") Duration readTimeout
) {}
