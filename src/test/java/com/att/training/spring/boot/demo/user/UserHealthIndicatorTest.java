package com.att.training.spring.boot.demo.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHealthIndicatorTest {

    private UserHealthIndicator userHealthIndicator;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        userHealthIndicator = new UserHealthIndicator(userRepository);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 3, 5})
    void whenRepositoryContainsAnOddUserCount_shouldReturnStatusUp(long userCount) {
        when(userRepository.count()).thenReturn(userCount);
        Status actualStatus = userHealthIndicator.health().getStatus();
        assertThat(actualStatus).isEqualTo(Status.UP);
    }

    @ParameterizedTest
    @ValueSource(longs = {2, 4, 6})
    void whenRepositoryContainsAnEvenUserCount_shouldReturnStatusDown(long userCount) {
        when(userRepository.count()).thenReturn(userCount);
        Status actualStatus = userHealthIndicator.health().getStatus();
        assertThat(actualStatus).isEqualTo(Status.DOWN);
    }
}
