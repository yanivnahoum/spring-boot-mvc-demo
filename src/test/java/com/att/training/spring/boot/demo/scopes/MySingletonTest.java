package com.att.training.spring.boot.demo.scopes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MySingleton.class, MyPrototype.class})
class MySingletonTest {

    @Autowired
    private MySingleton mySingleton;

    @Test
    void getPrototypeOrdinal() {
        for (var i = 1; i <= 10; i++) {
            assertThat(mySingleton.getPrototypeOrdinal()).isEqualTo(i);
        }
    }
}