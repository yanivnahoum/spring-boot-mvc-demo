package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("async/users")
@RequiredArgsConstructor
public class AsyncUserController {

    private final UserService userService;

    @GetMapping
    public CompletableFuture<List<User>> fetchAll() {
        return userService.fetchAllAsync();
    }
}