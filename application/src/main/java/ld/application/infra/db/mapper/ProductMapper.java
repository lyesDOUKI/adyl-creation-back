package ld.application.infra.db.mapper;

import ld.application.infra.db.entity.ProductEntity;
import ld.application.infra.db.entity.ProductPhotoEntity;
import ld.domain.features.product.model.ProductColor;
import ld.domain.features.product.model.ProductPhoto;
import ld.domain.features.product.model.ProductSnapshot;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProductMapper {

    private ProductMapper() {}

    // 1. Entity JPA -> Domain Snapshot (Lecture)
    public static ProductSnapshot toSnapshot(ProductEntity entity) {
        List<ProductColor> colors = entity.getColors() != null
                ? entity.getColors().stream().map(ProductColor::new).toList()
                : List.of();

        List<ProductPhoto> photos = entity.getPhotos() != null
                ? entity.getPhotos().stream()
                .sorted(Comparator.comparingInt(ProductPhotoEntity::getPosition))
                .map(ProductPhotoMapper::toDomain)
                .toList()
                : List.of();

        return new ProductSnapshot(
                entity.getId(),
                entity.getName(),
                entity.getUnitPrice(),
                colors,
                entity.getStatus(),
                photos
        );
    }

    public static ProductEntity toEntity(ProductSnapshot snapshot) {
        var entity = new ProductEntity();
        entity.setId(snapshot.productId());
        updateEntity(snapshot, entity);
        return entity;
    }

    public static void updateEntity(ProductSnapshot snapshot, ProductEntity targetEntity) {
        targetEntity.setName(snapshot.name());
        targetEntity.setUnitPrice(snapshot.price());
        targetEntity.setStatus(snapshot.productStatus());

        // Synchronisation des couleurs (@ElementCollection)
        if (snapshot.colors() != null) {
            Set<String> newColors = snapshot.colors().stream()
                    .map(ProductColor::value)
                    .collect(Collectors.toSet());
            targetEntity.getColors().clear();
            targetEntity.getColors().addAll(newColors);
        }

        // Synchronisation des photos (@OneToMany + orphanRemoval)
        if (snapshot.photos() != null) {
            updatePhotos(snapshot.photos(), targetEntity);
        }
    }

    private static void updatePhotos(List<ProductPhoto> photoSnapshots, ProductEntity productEntityEntity) {
        Map<UUID, ProductPhotoEntity> existingPhotosById = productEntityEntity.getPhotos().stream()
                .collect(Collectors.toMap(ProductPhotoEntity::getId, Function.identity()));

        Set<UUID> snapshotPhotoIds = photoSnapshots.stream()
                .map(ProductPhoto::id)
                .collect(Collectors.toSet());


        productEntityEntity.getPhotos().removeIf(entity -> !snapshotPhotoIds.contains(entity.getId()));

        // Ajout ou mise à jour des photos
        for (var photoDomain : photoSnapshots) {
            ProductPhotoEntity existingPhoto = existingPhotosById.get(photoDomain.id());

            if (existingPhoto != null) {
                existingPhoto.setPosition(photoDomain.position());
                existingPhoto.setStorageKey(photoDomain.storageKey());
            } else {
                ProductPhotoEntity newPhotoEntity = ProductPhotoMapper.toEntity(photoDomain, productEntityEntity);
                productEntityEntity.getPhotos().add(newPhotoEntity);
            }
        }
    }
}