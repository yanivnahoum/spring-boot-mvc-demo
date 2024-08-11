package com.att.training.spring.boot.demo.scopes;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@ExtendWith(SpringExtension.class)
@Import(MyPrototype.class)
@TestInstance(PER_METHOD)
@TestMethodOrder(OrderAnnotation.class)
class MyPrototypeTestWithSpringExtensionPerMethod {

    @Autowired
    private MyPrototype myPrototype;

    private MyPrototype firstTestInstance;
    @Order(1)
    @Test
    void firstTest() {
        firstTestInstance = myPrototype;
    }

    @Order(2)
    @Test
    void springBootTest_shouldReinjectDependencies_BeforeTest() {
        assertThat(firstTestInstance).isNotSameAs(myPrototype);
    }
}

@ExtendWith(SpringExtension.class)
@Import(MyPrototype.class)
@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class MyPrototypeTestWithSpringExtensionPerClass {

    @Autowired
    private MyPrototype myPrototype;

    private MyPrototype firstTestInstance;
    @Order(1)
    @Test
    void firstTest() {
        firstTestInstance = myPrototype;
    }

    @Order(2)
    @Test
    void springBootTest_shouldReinjectDependencies_BeforeTest() {
        assertThat(firstTestInstance).isSameAs(myPrototype);
    }
}

@SpringBootTest
@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class MyPrototypeTestSpringBootTest {

    @Autowired
    private MyPrototype myPrototype;

    private MyPrototype firstTestInstance;
    @Order(1)
    @Test
    void firstTest() {
        firstTestInstance = myPrototype;
    }

    @Order(2)
    @Test
    void springBootTest_shouldReinjectDependencies_BeforeTest() {
        assertThat(firstTestInstance).isNotSameAs(myPrototype);
    }
}

@SpringBootTest
@AllArgsConstructor
@TestConstructor(autowireMode = ALL)
@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class MyPrototypeTestSpringBootTestWithConstructor {

    private final MyPrototype myPrototype;
    private MyPrototype firstTestInstance;

    @Order(1)
    @Test
    void firstTest() {
        firstTestInstance = myPrototype;
    }

    @Order(2)
    @Test
    void springBootTestConstructor_isExecutedOnce() {
        assertThat(firstTestInstance).isSameAs(myPrototype);
    }
}
