package com.doublelizard.intrinsic;

import java.util.function.Consumer;
import java.util.function.Function;

public class Property<T> implements AutoCloseable {

    private final Holder<T> holder;
    private final Setter<T> setter;
    public final Event<Consumer<T>> valueChangeEvent;

    public Property(T value,Holder<T> holder, Setter<T> setter, Event<Consumer<T>> valueChangeEvent) {
        this.holder = holder;
        this.setter = setter;
        holder.value = value;
        this.valueChangeEvent = valueChangeEvent;
    }

    public void close() throws Exception {
        if (holder instanceof AutoCloseable) ((AutoCloseable) holder).close();
        if (setter instanceof AutoCloseable) ((AutoCloseable) setter).close();
    }

    public void set(T object){
        setter.set(object,this);
    }

    public T get(){
        return holder.get();
    }

    public void apply(Function<T,T> function){
        setter.apply(function,this);
    }

    protected static class Setter<T>{
        public void set(T obj, Property<T> property){
            property.holder.set(obj);
        }
        public void apply(Function<T,T> function, Property<T> property){
            property.holder.apply(function);
        }
    }

    protected static class Holder<T>{
        private T value;
        T get(){
            return value;
        }
        void set(T value){
            this.value = value;
        }
        void apply(Function<T,T> function){
            value = function.apply(value);
        }
    }

    protected static class WriteSafeHolder<T> extends Holder<T>{
        @Override
        void set(T value) {
            synchronized (this) {
                super.set(value);
            }
        }

        @Override
        void apply(Function<T, T> function) {
            synchronized (this) {
                super.apply(function);
            }
        }
    }

    protected static class AtomicHolder<T> extends WriteSafeHolder<T>{
        @Override
        T get() {
            synchronized (this){
                return super.get();
            }
        }
    }

    protected static class EventSetter<T> extends Setter<T>{
        @Override
        public synchronized void set(T obj, Property<T> property) {
            super.set(obj, property);
            property.valueChangeEvent.eventHandle.accept(obj);
        }

        @Override
        public synchronized void apply(Function<T, T> function, Property<T> property) {
            super.apply(function, property);
            property.valueChangeEvent.eventHandle.accept(property.get());
        }
    }


    static <T>Function<Event<Consumer<T>>,Consumer<T>> makeValueChangeEventAdapter(){
        return new Function<Event<Consumer<T>>, Consumer<T>>() {
            public Consumer<T> apply(final Event<Consumer<T>> consumerEvent) {
                return new Consumer<T>() {
                    public void accept(final T t) {
                        consumerEvent.accept(new Consumer<Consumer<T>>() {
                            public void accept(Consumer<T> tConsumer) {
                                tConsumer.accept(t);
                            }
                        });
                    }
                };
            }
        };
    }
}
