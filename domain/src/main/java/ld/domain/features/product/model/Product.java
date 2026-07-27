package ld.domain.features.product.model;

import ld.domain.valueObjects.Price;
import ld.standard.lib.AggregateRoot;
import ld.standard.lib.Snapshottable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Product extends AggregateRoot<UUID, ProductEvent> implements Snapshottable<ProductSnapshot> {

    private final String name;
    private final Price price;
    private final List<ProductColor> colors;
    private final ProductStatus productStatus;

    private Product(String name, Price price, List<ProductColor> colors) {
        setId(UUID.randomUUID());
        this.name = name;
        this.price = price;
        this.colors = colors;
        productStatus = ProductStatus.UNAVAILABLE;
        addDomainEvent(new ProductCreated(getId()));
    }

    public static Product create(String name, BigDecimal price, List<String> colors) {
        return new Product(name, new Price(price), colors.stream().map(ProductColor::new).toList());
    }

    @Override
    public ProductSnapshot toSnapshot() {
        return new ProductSnapshot(getId(), this.name,
                this.price.value(),
                this.colors,
                this.productStatus);
    }
}
