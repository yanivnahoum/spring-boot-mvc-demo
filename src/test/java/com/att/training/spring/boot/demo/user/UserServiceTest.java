package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.errors.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final User johnDoe = new User(17L, "John", "Doe", 30);
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        userService = new UserService(userRepository);
    }

    @Test
    void givenNoUsersExists_whenFetchUser_shouldThrowUserNotFoundException() {
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userService.fetch(101L));
    }

    @Test
    void givenUser17Exists_whenFindById17_shouldReturnUser17() {
        when(userRepository.findById(johnDoe.getId())).thenReturn(Optional.of(johnDoe));
        User user = userService.fetch(johnDoe.getId());
        assertThat(user).isEqualTo(johnDoe);
    }

    @Test
    void givenUser17Exists_whenFindById18_shouldThrowUserNotFoundException() {
        lenient().when(userRepository.findById(johnDoe.getId())).thenReturn(Optional.of(johnDoe));
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userService.fetch(18L));
    }
}