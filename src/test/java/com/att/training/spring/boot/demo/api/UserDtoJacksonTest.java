package com.att.training.spring.boot.demo.api;

import com.att.training.spring.boot.demo.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJacksonTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void given2PojosWithSameFieldsAndAtLeastOneIsImmutable_whenConvert_thenJacksonMapsBetweenThem() {
        User user = new User(100L, "Yaniv", "Nahoum", 40);
        UserDto userDto = mapper.convertValue(user, UserDto.class);
        assertThat(userDto).usingRecursiveComparison().isEqualTo(user);
    }
}