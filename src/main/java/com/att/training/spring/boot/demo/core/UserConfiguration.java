package com.att.training.spring.boot.demo.core;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.att.training.spring.boot.demo.api.User;
import com.google.common.base.MoreObjects;

@Component
@ConfigurationProperties
@Validated
public class UserConfiguration {
    
    @Valid
    @NotNull
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("users", users)
                .toString();
    }   
}
