package ld.domain.features.product;

import ld.domain.features.product.model.ProductSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InMemoryProductRepository implements ProductCreator, ProductChecker {

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

    public void addProduct(ProductSnapshot productSnapshot) {
        products.add(productSnapshot);
    }

    public int count() {
        return products.size();
    }

}