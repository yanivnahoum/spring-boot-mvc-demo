package com.att.training.spring.boot.demo.datetime;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.ACCEPTED;

@RequestMapping("jsr310")
@RestController
@Slf4j
public class DateTimeController {

    private final Clock clock;

    public DateTimeController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping
    public Jsr310 get() {
        var jsr310 = new Jsr310();
        jsr310.setInstant(Instant.now(clock));
        jsr310.setLocalDateTime(LocalDateTime.now(clock));
        return jsr310;
    }

    @PostMapping
    @ResponseStatus(ACCEPTED)
    public void post(@RequestBody Jsr310 jsr310) {
        log.info("#post - {}", jsr310);
    }
}

@Data
class Jsr310 {
    private Instant instant;
    private LocalDateTime localDateTime;
}
