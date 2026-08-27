package ld.domain.features.product.photos;

import ld.domain.features.product.model.ProductPhotoSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryAddProductPhotosRepository implements AddProductPhotosRepository {

    private final Map<UUID, ProductPhotoSnapshot> products = new HashMap<>();
    private int updateCount = 0;

    void addProduct(ProductPhotoSnapshot snapshot) {
        this.products.put(snapshot.productSnapshot().productId(), snapshot);
    }

    @Override
    public Optional<ProductPhotoSnapshot> findById(UUID productId) {
        return Optional.ofNullable(this.products.get(productId));
    }

    @Override
    public void execute(ProductPhotoSnapshot productPhotoSnapshot) {
        this.updateCount++;
    }

    int countUpdates() {
        return this.updateCount;
    }

}