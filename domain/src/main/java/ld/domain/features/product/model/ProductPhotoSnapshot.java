package ld.domain.features.product.model;

import java.util.List;

public record ProductPhotoSnapshot(ProductSnapshot productSnapshot, List<ProductPhoto> photos) {
}
