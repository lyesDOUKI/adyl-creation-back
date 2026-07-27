package ld.domain.features.product;

import ld.domain.features.product.model.ProductSnapshot;
import ld.domain.features.product.model.ProductStatus;
import ld.domain.features.shared.ProductSnapshotTestBuilder;

import java.math.BigDecimal;
import java.util.*;

public class InMemoryGetProductRepository implements GetProductRepository {

    private final Map<UUID, ProductSnapshot> products = new HashMap<>();

    @Override
    public List<ProductSnapshot> getAllBy(Collection<UUID> productsId) {
        return productsId.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public void addProduct(UUID productId, BigDecimal price) {
        products.put(productId, ProductSnapshotTestBuilder.aProduct()
                .withId(productId).withPrice(price).build());
    }

    public void addProduct(UUID productId, BigDecimal price, ProductStatus productStatus) {
        products.put(productId, ProductSnapshotTestBuilder.aProduct()
                .withId(productId).withPrice(price)
                .withStatus(productStatus).build());
    }

    public void addProduct(UUID productId, BigDecimal price, String productName) {
        products.put(productId, ProductSnapshotTestBuilder.aProduct()
                .withId(productId).withPrice(price)
                .withName(productName).build());
    }

}
