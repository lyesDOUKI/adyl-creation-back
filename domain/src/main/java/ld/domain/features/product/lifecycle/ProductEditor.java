package ld.domain.features.product.lifecycle;

import ld.domain.features.product.model.ProductSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface ProductEditor {
    Optional<ProductSnapshot> findById(UUID productId);
    void save(ProductSnapshot ProductSnapshot);
}
