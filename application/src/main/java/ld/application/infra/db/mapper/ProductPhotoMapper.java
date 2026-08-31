package ld.application.infra.db.mapper;

import ld.application.infra.db.entity.Product;
import ld.application.infra.db.entity.ProductPhotoEntity;
import ld.domain.features.product.model.ProductPhoto;

import java.time.Instant;

public final class ProductPhotoMapper {

    private ProductPhotoMapper() {}

    public static ProductPhoto toDomain(ProductPhotoEntity entity) {
        return new ProductPhoto(
                entity.getId(),
                entity.getStorageKey(),
                entity.getPosition()
        );
    }

    public static ProductPhotoEntity toEntity(ProductPhoto photo, Product productEntity) {
        ProductPhotoEntity entity = new ProductPhotoEntity();
        entity.setId(photo.id());
        entity.setStorageKey(photo.storageKey());
        entity.setPosition(photo.position());
        entity.setCreatedAt(Instant.now());
        entity.setProduct(productEntity);
        return entity;
    }
}