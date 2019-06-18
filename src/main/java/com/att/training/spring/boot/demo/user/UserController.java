package com.att.training.spring.boot.demo.user;

import com.att.training.spring.boot.demo.api.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ObjectMapper mapper;

    @GetMapping("{id}")
    public UserDto fetch(@PathVariable @Positive long id) {
        return toDto(userService.fetch(id));
    }

    @GetMapping
    public List<UserDto> fetchAll() {
        return userService.fetchAll()
                          .stream()
                          .map(this::toDto)
                          .collect(toList());
    }

    @PutMapping
    @ResponseStatus(NO_CONTENT)
    public void update(@Valid @RequestBody UserDto user) {
        userService.update(toEntity(user));
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable @Positive long id) {
        userService.delete(id);
    }

    private UserDto toDto(User user) {
        return mapper.convertValue(user, UserDto.class);
    }

    private User toEntity(UserDto userDto) {
        return mapper.convertValue(userDto, User.class);
    }
}
