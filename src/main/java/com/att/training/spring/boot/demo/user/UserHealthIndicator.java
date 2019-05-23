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
//        log.info("#health - CRLF\r\n2019-03-04 15:28:37,558 INFO  [main] c.a.t.s.b.d.u.UserHealthIndicator #health - ended");
//        log.info("#health - LF only\n2019-03-04 15:28:37,558 INFO  [main] c.a.t.s.b.d.u.UserHealthIndicator #health - ended");
//        log.error("An error occurred: ", new IllegalArgumentException("Oops!"));
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