package com.att.training.spring.boot.demo.datetime;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

public record CustomJsr310(Instant instant,
                           @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime localDateTime) {}
