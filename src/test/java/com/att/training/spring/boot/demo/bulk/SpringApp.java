package com.att.training.spring.boot.demo.bulk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(escapeCharacter = '@')
@SpringBootApplication(exclude = HazelcastAutoConfiguration.class)
public class SpringApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class);
    }
}
