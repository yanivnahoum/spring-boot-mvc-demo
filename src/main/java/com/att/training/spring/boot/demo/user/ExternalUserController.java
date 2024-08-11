package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/external/users")
@RequiredArgsConstructor
public class ExternalUserController {

    private final ExternalUserConfiguration users;

    @GetMapping
    public List<User> fetchAll() {
        return List.copyOf(users.getUsers());
    }
}
