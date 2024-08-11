package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Repository
public class UserRepository {
    
    private final Map<Long, User> users;

    public UserRepository(UserConfiguration userConfig) {
        users = userConfig.getUsers()
            .stream()
            .collect(toMap(User::getId, Function.identity()));
    }

    public void save(User user) {
        users.put(user.getId(), user);
    }
    
    public User delete(long id) {
        return users.remove(id);
    }
    
    public Optional<User> find(long id) {
        var user = users.get(id);
        return Optional.ofNullable(user);
    }
    
    /**
     * @return an immutable list of all users.
     */
    public List<User> findAll() {
        return List.copyOf(users.values());
    }
}