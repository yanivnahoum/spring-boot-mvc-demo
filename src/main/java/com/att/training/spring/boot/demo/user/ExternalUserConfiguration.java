package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

//@Component
//@PropertySource(factory = YamlPropertySourceFactory.class, value = "${app.config.dir:classpath:}external.yml")
@ConfigurationProperties(prefix = "ext")
@Validated
@Data
public class ExternalUserConfiguration {

    @Valid
    private final List<@NotNull User> users = new ArrayList<>();
}