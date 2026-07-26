package ld.domain.features.product;

import ld.domain.features.product.model.ProductSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GetProductRepository {
    List<ProductSnapshot> getAllBy(Collection<UUID> productsId);
}
