package ld.domain.features.shared;

import ld.domain.features.product.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductSnapshotTestBuilder {

    private UUID id = UUID.randomUUID();
    private String name = "default product";
    private BigDecimal price = BigDecimal.TEN;
    private List<ProductColor> colors = List.of(new ProductColor("noir"));
    private ProductStatus status = ProductStatus.AVAILABLE;
    private ProductCategory productCategory = ProductCategory.ACCESSORIES;
    private List<ProductPhoto> photos = new ArrayList<>();

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

    public ProductSnapshotTestBuilder withColors(List<ProductColor> colors) {
        this.colors = colors;
        return this;
    }

    public ProductSnapshotTestBuilder withStatus(ProductStatus status) {
        this.status = status;
        return this;
    }

    public ProductSnapshotTestBuilder withPhotos(List<ProductPhoto> photos) {
        this.photos = photos;
        return this;
    }

    public ProductSnapshotTestBuilder withCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
        return this;
    }
    public ProductSnapshot build() {
        return new ProductSnapshot(id, name, price, colors, status, photos, productCategory);
    }
}
