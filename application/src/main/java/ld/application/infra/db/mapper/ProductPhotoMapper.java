package ld.application.infra.db.mapper;

import ld.application.infra.db.entity.Product;
import ld.application.infra.db.entity.ProductPhotoEntity;
import ld.domain.features.product.model.ProductPhoto;
import ld.domain.features.product.model.ProductPhotoSnapshot;

import java.util.Comparator;
import java.util.List;

public class ProductPhotoMapper {

    private ProductPhotoMapper() {}

    public static ProductPhotoEntity from(ProductPhoto photo, Product product) {
        return new ProductPhotoEntity(
                photo.id(),
                photo.storageKey(),
                photo.position(),
                product
        );
    }

    public static ProductPhoto toDomain(ProductPhotoEntity entity) {
        return new ProductPhoto(
                entity.getId(),
                entity.getStorageKey(),
                entity.getPosition()
        );
    }

    public static ProductPhotoSnapshot toSnapshot(Product product) {
        List<ProductPhoto> photos = product.getPhotos().stream()
                .sorted(Comparator.comparingInt(ProductPhotoEntity::getPosition))
                .map(ProductPhotoMapper::toDomain)
                .toList();

        return new ProductPhotoSnapshot(ProductMapper.toSnapshot(product), photos);
    }
}
