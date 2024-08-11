package com.att.training.spring.boot.demo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    @Override
    public Health health() {
        int userCount = userRepository.findAll().size();
        if (isEven(userCount)) {
            return Health.down()
                         .withDetail("User count", userCount)
                         .build();
        }
        return Health.up().build();
    }

    private static boolean isEven(int userCount) {
        return userCount % 2 == 0;
    }
}
