package ld.domain.features.product.photos;

import ld.domain.features.product.model.ProductPhotoSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface AddProductPhotosRepository {
    Optional<ProductPhotoSnapshot> findById(UUID productId);
    void execute(ProductPhotoSnapshot productPhotoSnapshot);
}
