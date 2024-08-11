package com.att.training.spring.boot.demo.config;

import com.att.training.spring.boot.demo.user.UserProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.time.Clock;
import java.time.format.DateTimeFormatter;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class AppConfig {

    @Bean
    CommandLineRunner configurationPrinter(UserProperties userConfiguration) {
        return args -> log.info("#configurationPrinter - {}", userConfiguration);
    }

    @Bean
    CommandLineRunner secretPrinter(@Value("${db.user:inline-user}") String dbUser, @Value("${db.password:inline-password}") String dbPassword) {
        return args -> log.info("#Secrets: db.user={}, db.password={}", dbUser, dbPassword);
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

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    //@Bean
    public FormattingConversionService conversionService() {
        var conversionService = new DefaultFormattingConversionService(false);
        conversionService.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());
        var registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        registrar.registerFormatters(conversionService);

        return conversionService;
    }
}