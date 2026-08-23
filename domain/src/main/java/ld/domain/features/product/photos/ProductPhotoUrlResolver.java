package ld.domain.features.product.photos;

import java.util.UUID;

public interface ProductPhotoUrlResolver {
    String resolve(UUID productId, String storageKey);
}
