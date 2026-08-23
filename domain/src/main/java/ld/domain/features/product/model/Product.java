package ld.domain.features.product.model;

import ld.domain.valueObjects.Price;
import ld.standard.lib.AggregateRoot;
import ld.standard.lib.Snapshottable;
import ld.standard.lib.validation.Result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class Product extends AggregateRoot<UUID, ProductEvent> implements Snapshottable<ProductSnapshot> {

    private final String name;
    private final Price price;
    private final List<ProductColor> colors;
    private  List<ProductPhoto> photos;
    private final ProductStatus productStatus;

    private Product(String name, Price price, List<ProductColor> colors) {
        setId(UUID.randomUUID());
        this.name = name;
        this.price = price;
        this.colors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        this.photos = new ArrayList<>();
        productStatus = ProductStatus.AVAILABLE;
        addDomainEvent(new ProductCreated(getId()));
    }

    private Product(UUID id, String name, Price price, List<ProductColor> colors,
                    ProductStatus productStatus, List<ProductPhoto> photos) {
        setId(id);
        this.name = name;
        this.price = price;
        this.colors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        this.productStatus = productStatus;
        this.photos = photos == null ? new ArrayList<>() : new ArrayList<>(photos);
    }

    public static Product create(String name, BigDecimal price, List<String> colors) {
        return new Product(name, new Price(price), colors.stream().map(ProductColor::new).toList());
    }

    public static Product from(ProductPhotoSnapshot snapshot) {
        return new Product(
                snapshot.productSnapshot().productId(),
                snapshot.productSnapshot().name(),
                new Price(snapshot.productSnapshot().price()),
                snapshot.productSnapshot().colors(),
                snapshot.productSnapshot().productStatus(),
                snapshot.photos()
        );
    }

    public Result<Void> addPhotos(List<ProductPhoto> newPhotos) {
        if (newPhotos == null || newPhotos.isEmpty()) {
            return Result.ok();
        }
        this.photos = Stream.concat(this.photos.stream(), newPhotos.stream()).toList();
        return Result.ok();
    }
    @Override
    public ProductSnapshot toSnapshot() {
        return new ProductSnapshot(getId(),
                this.name,
                this.price.value(),
                this.colors,
                this.productStatus);
    }

    public List<ProductPhoto> getPhotos() {
        return Collections.unmodifiableList(this.photos);
    }
}
