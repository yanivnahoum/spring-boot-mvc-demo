package com.att.training.spring.boot.demo.config;

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;

@Configuration(proxyBeanMethods = false)
public class ErrorConfig {
    @Bean
    ErrorViewResolver viewResolver() {
        return (request, status, model) ->  {
            var modelAndView = new ModelAndView();
            modelAndView.setViewName("/fallback-error.html");
            return modelAndView;
        };
    }
}
