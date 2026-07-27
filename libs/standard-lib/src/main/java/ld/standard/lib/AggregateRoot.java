package ld.standard.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AggregateRoot <T, U extends DomainEvent>{
    private T id;
    private final List<U> domainEvents = new ArrayList<>();

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public List<U> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    public void addDomainEvent(U event) {
        this.domainEvents.add(event);
    }
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
