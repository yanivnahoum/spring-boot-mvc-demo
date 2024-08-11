package com.att.training.spring.boot.demo.api;

import lombok.Value;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@ConstructorBinding
@Value
public class User {

    @Min(1)
    long id;
    @NotEmpty
    String firstName;
    @NotEmpty
    String lastName;
    @Range(min = 18, max = 120)
    int age;
}