package com.att.training.spring.boot.demo.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    @Override
    public Health health() {
        log.info("#health - starting now");
        long userCount = userRepository.count();
        if (isEven(userCount)) {
            return Health.down()
                         .withDetail("User count", userCount)
                         .build();
        }
        return Health.up().build();
    }

    private static boolean isEven(long userCount) {
        return userCount % 2 == 0;
    }
}