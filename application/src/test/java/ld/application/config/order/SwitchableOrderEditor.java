package ld.application.config.order;

import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.OrderSnapshot;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SwitchableOrderEditor implements OrderEditor {
    private final OrderEditor delegate;
    private final AtomicReference<Consumer<OrderSnapshot>> afterCreateHook =
            new AtomicReference<>();

    public SwitchableOrderEditor(OrderEditor delegate) {
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
    public void save(OrderSnapshot snapshot) {
        delegate.save(snapshot);
        var hook = afterCreateHook.getAndSet(null);
        if (hook != null) {
            hook.accept(snapshot);
        }
    }

    @Override
    public Optional<OrderSnapshot> findById(UUID orderId) {
        return delegate.findById(orderId);
    }
}
