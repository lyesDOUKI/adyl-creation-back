package ld.application.config.product;

import ld.domain.features.product.lifecycle.ProductEditor;
import ld.domain.features.product.model.ProductSnapshot;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SwitchableProductEditor implements ProductEditor {
    private final ProductEditor delegate;
    private final AtomicReference<Consumer<ProductSnapshot>> afterCreateHook =
            new AtomicReference<>();

    public SwitchableProductEditor(ProductEditor delegate) {
        this.delegate = delegate;
    }

    public void failAfterCreateWith(RuntimeException ex) {
        afterCreateHook.set(snapshot -> { throw ex; });
    }

    public void afterCreate(Consumer<ProductSnapshot> hook) {
        afterCreateHook.set(hook);
    }

    public void reset() {
        afterCreateHook.set(null);
    }
    @Override
    public Optional<ProductSnapshot> findById(UUID productId) {
        return delegate.findById(productId);
    }

    @Override
    public void save(ProductSnapshot ProductSnapshot) {
        delegate.save(ProductSnapshot);
        var hook = afterCreateHook.getAndSet(null);
        if (hook != null) {
            hook.accept(ProductSnapshot);
        }
    }
}
