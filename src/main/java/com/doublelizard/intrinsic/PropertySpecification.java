package com.doublelizard.intrinsic;

import java.util.function.Consumer;

public class PropertySpecification<T> {
    Factory<Property.Holder<T>> holderFactory;
    Factory<Property.Setter<T>> setterFactory;
    Factory<Event<Consumer<T>>> valueChangeEventFactory;

    protected interface Factory<T>{
        public T get();
    }
}