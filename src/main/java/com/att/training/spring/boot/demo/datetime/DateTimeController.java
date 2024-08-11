package com.att.training.spring.boot.demo.datetime;


import com.att.training.spring.boot.demo.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
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
        return new Jsr310(Instant.now(clock), LocalDateTime.now(clock));
    }

    @GetMapping("v2")
    public Jsr310 getV2(@RequestParam Instant instant, @RequestParam LocalDateTime datetime) {
        return new Jsr310(instant, datetime);
    }

    @GetMapping("v3")
    public Jsr310 getV3(@RequestParam Instant instant, @RequestParam @DateTimeFormat(iso = DATE_TIME) LocalDateTime datetime) {
        return new Jsr310(instant, datetime);
    }

    /**
     * @see AppConfig#conversionService() to configure a custom datetime pattern
     */
    @GetMapping("v4")
    public Jsr310 getV4(@RequestParam Instant instant, @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime datetime) {
        return new Jsr310(instant, datetime);
    }

    @PostMapping
    @ResponseStatus(ACCEPTED)
    public void post(@RequestBody Jsr310 jsr310) {
        log.info("#post - {}", jsr310);
    }
}
