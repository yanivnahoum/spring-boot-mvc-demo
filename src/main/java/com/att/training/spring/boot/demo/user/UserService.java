package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import com.att.training.spring.boot.demo.errors.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User fetch(long id) {
        return findUser(id);
    }

    public List<User> fetchAll() {
        return userRepository.findAll();
    }

    public void update(User user) {
        findUser(user.getId());
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
