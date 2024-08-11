package com.att.training.spring.boot.demo.user;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.time.Duration;

@ConstructorBinding
@ConfigurationProperties(prefix = "app.http-service")
@Validated
@Value
public class HttpServiceProperties {

    @NotEmpty
    String url;
    Duration connectTimeout;
    Duration readTimeout;
    int maxRetries;

    public HttpServiceProperties(
            String url,
            @DefaultValue("30s") Duration connectTimeout,
            @DefaultValue("30s") Duration readTimeout,
            @DefaultValue("5") int maxRetries
    ) {
        this.url = url;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxRetries = maxRetries;
    }
}

