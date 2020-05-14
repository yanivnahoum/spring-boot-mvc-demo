package com.att.training.spring.boot.demo.tc;

import com.att.training.spring.boot.demo.user.UserRepository;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test demonstrates that @Transactional (on base class {@link MySqlSingletonContainer}
 * auto-rollbacks at the end of each test.
 */
@TestMethodOrder(OrderAnnotation.class)
class UserRepositoryTest extends MySqlSingletonContainer {

    @Autowired
    private UserRepository userRepository;

    @Order(1)
    @Test
    void first() {
        assertThat(userRepository.count()).isEqualTo(3);
    }

    @Order(2)
    @Test
    void second() {
        userRepository.deleteAll();
        assertThat(userRepository.count()).isZero();
    }

    @Order(3)
    @Test
    void third() {
        assertThat(userRepository.count()).isEqualTo(3);
    }
}
