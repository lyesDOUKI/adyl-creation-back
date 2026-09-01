package ld.application.shared.product;

import ld.application.infra.db.jooq.ProductQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductQueryTestBuilder {

    private UUID productId = UUID.randomUUID();
    private String name = "Produit test";
    private BigDecimal price = BigDecimal.TEN;
    private List<String> colors = new ArrayList<>();
    private List<String> photoStorageKeys = new ArrayList<>();
    private int numberOfOrders = 0;

    private ProductQueryTestBuilder() {
    }

    public static ProductQueryTestBuilder aProduct() {
        return new ProductQueryTestBuilder();
    }

    public ProductQueryTestBuilder withProductId(UUID productId) {
        this.productId = productId;
        return this;
    }

    public ProductQueryTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductQueryTestBuilder withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public ProductQueryTestBuilder withColors(String... colors) {
        this.colors = List.of(colors);
        return this;
    }

    public ProductQueryTestBuilder withPhotos(String... photoStorageKeys) {
        this.photoStorageKeys = List.of(photoStorageKeys);
        return this;
    }

    public ProductQueryTestBuilder withNumberOfOrders(int numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
        return this;
    }

    public ProductQuery build() {
        return new ProductQuery(productId, name, price, colors, photoStorageKeys, numberOfOrders);
    }
}
