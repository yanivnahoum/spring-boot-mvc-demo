package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.tc.MySqlSingletonContainer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test demonstrates that @Transactional (on base class {@link MySqlSingletonContainer}
 * auto-rollbacks at the end of each test.
 * {@link org.junit.jupiter.api.BeforeEach} and {@link org.junit.jupiter.api.AfterEach} participate in the transaction,
 * whereas {@link org.junit.jupiter.api.BeforeAll} and {@link org.junit.jupiter.api.AfterAll} <b>do not</b>.
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
