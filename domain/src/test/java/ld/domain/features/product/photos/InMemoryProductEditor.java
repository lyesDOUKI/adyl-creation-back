package ld.domain.features.product.photos;

import ld.domain.features.product.lifecycle.ProductEditor;
import ld.domain.features.product.model.ProductSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryProductEditor implements ProductEditor {

    private final Map<UUID, ProductSnapshot> products = new HashMap<>();
    private int updateCount = 0;

    void addProduct(ProductSnapshot snapshot) {
        this.products.put(snapshot.productId(), snapshot);
    }

    @Override
    public Optional<ProductSnapshot> findById(UUID productId) {
        return Optional.ofNullable(this.products.get(productId));
    }

    @Override
    public void save(ProductSnapshot productPhotoSnapshot) {
        this.updateCount++;
    }

    int countUpdates() {
        return this.updateCount;
    }

}