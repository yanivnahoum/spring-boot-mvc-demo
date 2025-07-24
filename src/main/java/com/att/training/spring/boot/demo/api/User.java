package com.att.training.spring.boot.demo.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;

public record User(
        @Positive long id,
        @NotEmpty String firstName,
        @NotEmpty String lastName,
        @Range(min = 18, max = 120) int age
) {}
