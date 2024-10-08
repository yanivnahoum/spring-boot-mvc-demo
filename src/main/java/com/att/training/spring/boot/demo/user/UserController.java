package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("{id}")
    public User fetch(@PathVariable @Positive long id) {
        return userService.fetch(id);
    }

    @GetMapping
    public List<User> fetchAll() {
        return userService.fetchAll();
    }

    @PutMapping
    @ResponseStatus(NO_CONTENT)
    public void update(@Valid @RequestBody User user) {
        userService.update(user);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable @Positive long id) {
        userService.delete(id);
    }
}
