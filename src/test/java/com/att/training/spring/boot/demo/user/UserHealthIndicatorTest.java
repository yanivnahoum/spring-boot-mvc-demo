package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserHealthIndicatorTest {

    private static final User JOHN_DOE = new User(17, "John", "Doe", 30);
    private UserHealthIndicator userHealthIndicator;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        userHealthIndicator = new UserHealthIndicator(userRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5})
    void whenRepositoryContainsAnOddUserCount_shouldReturnStatusUp(int listSize) {
        Mockito.when(userRepository.findAll()).thenReturn(buildListOfSize(listSize));
        Status actualStatus = userHealthIndicator.health().getStatus();
        assertThat(actualStatus).isEqualTo(Status.UP);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 6})
    void whenRepositoryContainsAnEvenUserCount_shouldReturnStatusDown(int listSize) {
        Mockito.when(userRepository.findAll()).thenReturn(buildListOfSize(listSize));
        Status actualStatus = userHealthIndicator.health().getStatus();
        assertThat(actualStatus).isEqualTo(Status.DOWN);
    }

    private List<User> buildListOfSize(int size) {
        var users = new ArrayList<User>();
        for (var i = 0; i < size; i++) {
            users.add(JOHN_DOE);
        }
        return users;
    }
}
