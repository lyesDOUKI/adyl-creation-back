package ld.lib;

public interface AggregateEventDispatcher<T extends DomainEvent> {
    void dispatch(T event);
}

