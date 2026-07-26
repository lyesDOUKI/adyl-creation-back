package ld.lib.helper.test;

import ld.lib.AggregateEventDispatcher;
import ld.lib.DomainEvent;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAggregateEventDispatcher<T extends DomainEvent> implements AggregateEventDispatcher<T> {

    private final List<T> dispatchedEvents = new ArrayList<>();

    @Override
    public void dispatch(T event) {
        dispatchedEvents.add(event);
    }

    public List<T> getDispatchedEvents() {
        return dispatchedEvents;
    }

    public int count() {
        return dispatchedEvents.size();
    }
}

