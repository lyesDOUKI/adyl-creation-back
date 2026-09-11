package ld.application.config.product;

import ld.domain.features.product.ProductCreator;
import ld.domain.features.product.model.ProductSnapshot;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SwitchableProductCreator implements ProductCreator {
    private final ProductCreator delegate;
    private final AtomicReference<Consumer<ProductSnapshot>> afterCreateHook =
            new AtomicReference<>();

    public SwitchableProductCreator(ProductCreator delegate) {
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
    public void create(ProductSnapshot product) {
        delegate.create(product);
        var hook = afterCreateHook.getAndSet(null);
        if (hook != null) {
            hook.accept(product);
        }
    }
}
