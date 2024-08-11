package com.att.training.spring.boot.demo.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.http-service")
@Validated
public record HttpServiceProperties (
    @NotEmpty String url,
    @DefaultValue("30s") Duration connectTimeout,
    @DefaultValue("30s") Duration readTimeout,
    @DefaultValue("5") int maxRetries
) { }

