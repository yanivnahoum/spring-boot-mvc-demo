package com.att.training.spring.boot.demo.user;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("since the complete context requires an available mysql instance")
@SpringBootTest
class SpringContextTest {

    @Test
    void contextLoads() {
    }
}
