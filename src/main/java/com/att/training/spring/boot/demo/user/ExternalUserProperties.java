package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

// We use @ConfigurationPropertiesScan on main class instead of @Component
//@Component
// We don't need this starting from 2.5.x: using spring.config.import in instead
//@PropertySource(factory = YamlPropertySourceFactory.class, value = "${app.config.dir:classpath:}external.yml")
@ConfigurationProperties(prefix = "ext")
@Validated
@Data
public class ExternalUserProperties {

    @Valid
    private final List<@NotNull User> users = new ArrayList<>();
}