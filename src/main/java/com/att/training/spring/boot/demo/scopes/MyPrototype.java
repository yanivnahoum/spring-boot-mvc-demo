package com.att.training.spring.boot.demo.scopes;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
class MyPrototype {

    private static final AtomicInteger counter = new AtomicInteger();

    public MyPrototype() {
        counter.incrementAndGet();
    }

    public int getOrdinal() {
        return counter.get();
    }
}
