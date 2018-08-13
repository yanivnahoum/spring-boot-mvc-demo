package com.att.training.spring.boot.demo.controllers;

import com.att.training.spring.boot.demo.api.User;
import com.att.training.spring.boot.demo.core.UserRepository;
import com.att.training.spring.boot.demo.errors.UserNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("users")
public class UserController {
    
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @GetMapping("{id}")
    public User fetch(@PathVariable long id) {
        return findUser(id);
    }

    private User findUser(long id) {
        return userRepository.find(id)
                .orElseThrow(() -> new UserNotFoundException(Long.toString(id)));
    }    
    
    @GetMapping
    public List<User> fetchAll() {
        return userRepository.getAll();
    }   
    
    @PutMapping
    @ResponseStatus(NO_CONTENT)
    public void update(@NotNull @Valid @RequestBody User user) {
        findUser(user.getId());
        userRepository.add(user);
    }    

}
