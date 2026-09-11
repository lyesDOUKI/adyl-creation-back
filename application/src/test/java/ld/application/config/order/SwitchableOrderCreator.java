package ld.application.config.order;

import ld.domain.features.order.OrderCreator;
import ld.domain.features.order.model.OrderSnapshot;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SwitchableOrderCreator implements OrderCreator {
    private final OrderCreator delegate;
    private final AtomicReference<Consumer<OrderSnapshot>> afterCreateHook =
            new AtomicReference<>();

    public SwitchableOrderCreator(OrderCreator delegate) {
        this.delegate = delegate;
    }

    public void failAfterCreateWith(RuntimeException ex) {
        afterCreateHook.set(snapshot -> { throw ex; });
    }

    public void afterCreate(Consumer<OrderSnapshot> hook) {
        afterCreateHook.set(hook);
    }

    public void reset() {
        afterCreateHook.set(null);
    }

    @Override
    public void create(OrderSnapshot order) {
        delegate.create(order);
        var hook = afterCreateHook.getAndSet(null);
        if (hook != null) {
            hook.accept(order);
        }
    }
}
