package com.att.training.spring.boot.demo.api;

import com.google.common.base.MoreObjects;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Validated
public class User {

    @Min(1)
    private long id;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Min(18)
    @Max(120)
    private int age;

    public User() {
        // For spring data binder
    }

    public User(long id, String firstName, String lastName, int age) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("firstName", firstName)
                          .add("lastName", lastName)
                          .add("age", age)
                          .toString();
    }
}