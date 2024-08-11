package com.att.training.spring.boot.demo.scopes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Getter
@Component
class MySingleton {

    private final MyPrototype myPrototype1;
    private final MyPrototype myPrototype2;
    private final ObjectProvider<MyPrototype> myPrototypeProvider;

    public int getPrototypeOrdinal() {
        return myPrototypeProvider.getObject().getOrdinal();
    }
}
