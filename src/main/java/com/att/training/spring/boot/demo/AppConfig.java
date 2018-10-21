package com.att.training.spring.boot.demo;

import com.att.training.spring.boot.demo.user.UserConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
@Slf4j
class AppConfig {

    @Bean
    CommandLineRunner configurationPrinter(UserConfiguration userConfiguration) {
        return args -> log.info("#configurationPrinter - {}", userConfiguration);
    }

    @Bean
    CommonsRequestLoggingFilter requestLoggingFilter() {
        var loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(200);
        return loggingFilter;
    }
}
