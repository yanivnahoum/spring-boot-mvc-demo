package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "app")
@Validated
public @Data class UserProperties {
    
    @Valid
    @NotNull
    private List<@NotNull  User> users;
}

