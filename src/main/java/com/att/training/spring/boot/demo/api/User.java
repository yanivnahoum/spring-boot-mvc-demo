package com.att.training.spring.boot.demo.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

public record User(
        @Min(1) long id,
        @NotEmpty String firstName,
        @NotEmpty String lastName,
        @Range(min = 18, max = 120) int age
) {}