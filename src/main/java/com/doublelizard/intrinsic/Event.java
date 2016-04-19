package com.doublelizard.intrinsic;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

public class Event<T> {

    private final LinkedList<T> invocationList = new LinkedList<T>();

    private T contextInvocation;
    private Thread contextThread;
    private boolean canceled = false;

    public final T eventHandle;

    protected Event(Function<Event<T>,T> eventHandleProducer) {
        this.eventHandle = eventHandleProducer.apply(this);
    }

    synchronized void remove(T removable){
        invocationList.remove(removable);
    }

    /**
     * The event listeners will be called in the order provided by the given comparator.
     */
    synchronized void sort(final Comparator<T> comparator){
        invocationList.sort(comparator);
    }

    public synchronized void accept(Consumer<T> invoker){
        contextThread = Thread.currentThread();
        try {
            for (T invocation : invocationList) {
                contextInvocation = invocation;
                invoker.accept(invocation);
                if (canceled) {
                    canceled = false;
                    break;
                }
            }
        }
        finally {
            contextInvocation = null;
            contextThread = null;
        }
    }

    public synchronized void addListener(T listener){
        invocationList.add(listener);
    }

    private void validateAccess(){
        if (Thread.currentThread() != contextThread) throw new RuntimeException("Should only be called from an event listener.");
    }

    /**
     * Remove the event listener currently being called.
     */
    public void removeCurrentListener(){
        validateAccess();
        remove(contextInvocation);
    }

    /**
     * No remaining listeners will be called after this.
     */
    public void cancel(){
        validateAccess();
        canceled = true;
    }

    /**
     * Create a new event instance.
     * @param eventHandleProducer this is a function that is expected to produce the event handle.
     *                            The contract for the event handle is to produce an invocation function
     *                            in the form of a Consumer<T> and call the Event instances .accept(Consumer<T>) method with it.
     * @param <T> Invokable type. This should probably be a functional interface. Arguments passed to this are supposed to be passed to the invocations of the listeners.
     * @return the event instance.
     */
    public static <T> Event<T> make(Function<Event<T>,T> eventHandleProducer){
        return new Event<T>(eventHandleProducer);
    }

}
