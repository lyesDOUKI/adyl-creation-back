package ld.domain.features.product;

import ld.domain.features.product.model.ProductSnapshot;

public interface CreateProductRepository {
    boolean alreadyExists(String name);
    void create(ProductSnapshot product);
}
