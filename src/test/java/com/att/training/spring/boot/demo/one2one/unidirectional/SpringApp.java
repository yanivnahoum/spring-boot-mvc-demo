package com.att.training.spring.boot.demo.one2one.unidirectional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;

@SpringBootApplication(exclude = HazelcastAutoConfiguration.class)
public class SpringApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class);
    }
}

