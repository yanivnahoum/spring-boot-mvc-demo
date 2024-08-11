package com.att.training.spring.boot.demo.scopes;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
class MySingleton {

    private final ObjectProvider<MyPrototype> myPrototypeProvider;

    public MySingleton(ObjectProvider<MyPrototype> myPrototypeProvider) {
        this.myPrototypeProvider = myPrototypeProvider;
    }

    public int getPrototypeOrdinal() {
        return myPrototypeProvider.getObject().getOrdinal();
    }
}
