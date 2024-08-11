package com.att.training.spring.boot.demo.datetime;

import java.time.Instant;
import java.time.LocalDateTime;

public record Jsr310(Instant instant, LocalDateTime localDateTime) {}
