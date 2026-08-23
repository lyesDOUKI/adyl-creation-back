package ld.domain.features.product.model;

import java.util.UUID;

public record ProductPhoto(
        UUID id,
        String storageKey,
        int position
) {
    public static ProductPhoto create(UUID id, String storageKey) {
        return new ProductPhoto(id, storageKey, -1);
    }

    public ProductPhoto withPosition(int position) {
        return new ProductPhoto(id, storageKey, position);
    }
}