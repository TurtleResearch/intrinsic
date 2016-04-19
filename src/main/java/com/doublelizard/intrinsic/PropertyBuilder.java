package com.doublelizard.intrinsic;

import java.util.function.Consumer;

public final class PropertyBuilder<T> {
    PropertySpecification<T> specificationPrototype;
    private boolean emittedSpec;

    protected PropertyBuilder(){
        specificationPrototype = new PropertySpecification<T>();
    }

    public void setWriteSafe(){
        specificationPrototype.holderFactory = new PropertySpecification.Factory<Property.Holder<T>>() {
            public Property.Holder<T> get() {
                return new Property.WriteSafeHolder<T>();
            }
        };
    }

    public void setAtomicAccess(){
        specificationPrototype.holderFactory = new PropertySpecification.Factory<Property.Holder<T>>() {
            public Property.Holder<T> get() {
                return new Property.AtomicHolder<T>();
            }
        };
    }

    public void setEnableValueChangeEvent(){
        specificationPrototype.setterFactory = new PropertySpecification.Factory<Property.Setter<T>>() {
            public Property.Setter<T> get() {
                return new Property.EventSetter<T>();
            }
        };
        specificationPrototype.valueChangeEventFactory = new PropertySpecification.Factory<Event<Consumer<T>>>() {
            public Event<Consumer<T>> get() {
                return Event.make(Property.<T>makeValueChangeEventAdapter());
            }
        };
    }

    public Property<T> build(){
        return build(null);
    }

    public Property<T> build(T value){
        PropertySpecification<T> specification = emittedSpec?specificationPrototype:buildSpecification();
        return buildFromSpecification(specification,value);
    }

    public synchronized PropertySpecification<T> buildSpecification(){
        if (emittedSpec) throw new IllegalStateException("A single PropertyBuilder can only yield one specification.");
        if (specificationPrototype.holderFactory == null)
            specificationPrototype.holderFactory = new PropertySpecification.Factory<Property.Holder<T>>() {
                public Property.Holder<T> get() {
                    return new Property.Holder<T>();
                }
            };
        if (specificationPrototype.setterFactory == null){
            specificationPrototype.setterFactory = new PropertySpecification.Factory<Property.Setter<T>>() {
                public Property.Setter<T> get() {
                    return new Property.Setter<T>();
                }
            };
        }
        return specificationPrototype;
    }

    public static <T> PropertyBuilder<T> make(){
        return new PropertyBuilder<T>();
    }

    public static <T>Property<T> buildFromSpecification(PropertySpecification<T> specification, T value){
        return new Property<T>(value,specification.holderFactory.get(),specification.setterFactory.get(),specification.valueChangeEventFactory.get());
    }

    public static <T> Property<T> buildFromSpecification(PropertySpecification<T> specification){
        return buildFromSpecification(specification,null);
    }
}
