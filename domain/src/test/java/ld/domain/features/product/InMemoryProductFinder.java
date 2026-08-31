package ld.domain.features.product;

import ld.domain.features.product.model.ProductSnapshot;

import java.util.*;

public class InMemoryProductFinder implements ProductFinder {

    private final List<ProductSnapshot> products = new ArrayList<>();

    @Override
    public List<ProductSnapshot> findAllBy(Collection<UUID> productsId) {
        Set<UUID> ids = new HashSet<>(productsId);
        return products.stream()
                .filter(product -> ids.contains(product.productId()))
                .toList();
    }

    public void addProduct(ProductSnapshot productSnapshot) {
        products.add(productSnapshot);
    }
}
