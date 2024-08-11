package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.RandomDelay;
import com.att.training.spring.boot.demo.api.User;
import com.att.training.spring.boot.demo.errors.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@RandomDelay
public class UserService {

    private final UserRepository userRepository;
    private final Executor cpuTaskExecutor;

    public User fetch(long id) {
        return findUser(id);
    }

    @RandomDelay(min = 200, max = 400)
    public List<User> fetchAll() {
        return userRepository.findAll();
    }

    public CompletableFuture<List<User>> fetchAllAsync() {
        return CompletableFuture.supplyAsync(userRepository::findAll, cpuTaskExecutor);
    }

    public void update(User user) {
        findUser(user.id());
        userRepository.save(user);
    }

    public void delete(long id) {
        findUser(id);
        userRepository.delete(id);
    }

    private User findUser(long id) {
        return userRepository.find(id)
                             .orElseThrow(() -> new UserNotFoundException(Long.toString(id)));
    }
}
