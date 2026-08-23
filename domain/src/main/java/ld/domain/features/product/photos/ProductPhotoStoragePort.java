package ld.domain.features.product.photos;

import java.util.UUID;

public interface ProductPhotoStoragePort {
    String store(UUID productId, String fileName, byte[] content);
}