package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.errors.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User fetch(long id) {
        return findUser(id);
    }

    public List<User> fetchAll() {
        return newArrayList(userRepository.findAll());
    }

    public void update(User user) {
        findUser(user.getId());
        userRepository.save(user);
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }

    private User findUser(long id) {
        return userRepository.findById(id)
                             .orElseThrow(() -> new UserNotFoundException(Long.toString(id)));
    }
}
