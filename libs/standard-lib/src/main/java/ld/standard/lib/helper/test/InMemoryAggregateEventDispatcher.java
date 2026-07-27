package ld.standard.lib.helper.test;

import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.DomainEvent;

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

