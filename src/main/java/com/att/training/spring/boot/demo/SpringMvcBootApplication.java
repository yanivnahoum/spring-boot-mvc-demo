package com.att.training.spring.boot.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.att.training.spring.boot.demo.core.UserConfiguration;

@SpringBootApplication
public class SpringMvcBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringMvcBootApplication.class, args);
	}
	
	@Bean
	CommandLineRunner configurationPrinter(UserConfiguration userConfiguration) {
	    return args -> System.out.println(userConfiguration);
	}
	
    @Bean
    CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(200);
        return loggingFilter;
    } 	
}
