package com.att.training.spring.boot.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
public @Data class User {

    @Min(1)
    private long id;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Range(min = 18, max = 120)
    private int age;
}