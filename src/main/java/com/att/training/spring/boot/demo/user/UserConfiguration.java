package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Validated
public @Data class UserConfiguration {
    
    @Valid
    @NotNull
    private List<@NotNull  User> users;
}
