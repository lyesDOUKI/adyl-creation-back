package ld.domain.features.product;

import ld.domain.features.product.model.ProductSnapshot;
import ld.domain.features.shared.ProductSnapshotTestBuilder;

import java.util.ArrayList;
import java.util.List;

public class InMemoryCreateProductRepository implements CreateProductRepository {

    private final List<ProductSnapshot> products = new ArrayList<>();

    @Override
    public boolean alreadyExists(String name) {
        return products.stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(name));
    }

    @Override
    public void create(ProductSnapshot product) {
        products.add(product);
    }

    public void addProduct(String name) {
        products.add(ProductSnapshotTestBuilder.aProduct().withName(name).build());
    }

    public int count() {
        return products.size();
    }

    public ProductSnapshot getLast() {
        return products.getLast();
    }
}