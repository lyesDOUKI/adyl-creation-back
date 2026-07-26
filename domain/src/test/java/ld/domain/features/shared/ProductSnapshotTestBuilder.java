package ld.domain.features.shared;

import ld.domain.features.product.model.ProductSnapshot;
import ld.domain.features.product.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductSnapshotTestBuilder {

    private UUID id = UUID.randomUUID();
    private String name = "default product";
    private BigDecimal price = BigDecimal.TEN;
    private List<String> colors = List.of("noir");
    private ProductStatus status = ProductStatus.UNAVAILABLE;

    public static ProductSnapshotTestBuilder aProduct() {
        return new ProductSnapshotTestBuilder();
    }

    public ProductSnapshotTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    public ProductSnapshotTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductSnapshotTestBuilder withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public ProductSnapshotTestBuilder withColors(List<String> colors) {
        this.colors = colors;
        return this;
    }

    public ProductSnapshotTestBuilder withStatus(ProductStatus status) {
        this.status = status;
        return this;
    }

    public ProductSnapshot build() {
        return new ProductSnapshot(id, name, price, colors, status);
    }
}
