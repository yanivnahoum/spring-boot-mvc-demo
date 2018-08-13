package com.att.training.spring.boot.demo.core;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Repository;

import com.att.training.spring.boot.demo.api.User;
import com.google.common.collect.ImmutableList;

@Repository
public class UserRepository {
    
    private final Map<Long, User> users;

    public UserRepository(UserConfiguration userConfig) {
        users = userConfig.getUsers()
            .stream()
            .collect(toMap(User::getId, Function.identity()));
    }

    public User add(User user) {
        return users.put(user.getId(), user);
    }
    
    public User remove(long id) {
        return users.remove(id);
    }
    
    public Optional<User> find(long id) {
        User user = users.get(id);
        return Optional.ofNullable(user);
    }
    
    /**
     * @return an immutable list of all users.
     */
    public List<User> getAll() {
        return ImmutableList.copyOf(users.values());
    }
}