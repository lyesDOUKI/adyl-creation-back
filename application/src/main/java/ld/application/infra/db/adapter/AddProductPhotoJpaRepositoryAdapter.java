package ld.application.infra.db.adapter;

import ld.application.infra.db.entity.Product;
import ld.application.infra.db.entity.ProductPhotoEntity;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.mapper.ProductPhotoMapper;
import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.domain.features.product.photos.AddProductPhotosRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AddProductPhotoJpaRepositoryAdapter implements AddProductPhotosRepository {

    private final ProductJpaRepository productJpaRepository;

    public AddProductPhotoJpaRepositoryAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Optional<ProductPhotoSnapshot> findById(UUID productId) {
        return this.productJpaRepository.findById(productId)
                .map(ProductPhotoMapper::toSnapshot);
    }

    @Override
    public void execute(ProductPhotoSnapshot productPhotoSnapshot) {
        Product product = this.productJpaRepository.findById(productPhotoSnapshot.productSnapshot().productId())
                .orElseThrow(() -> new IllegalStateException(
                        "Product %s should exist for photo persistence".formatted(
                                productPhotoSnapshot.productSnapshot().productId()
                        )
                ));

        Set<UUID> existingIds = product.getPhotos().stream()
                .map(ProductPhotoEntity::getId)
                .collect(Collectors.toSet());

        productPhotoSnapshot.photos().stream()
                .filter(photo -> !existingIds.contains(photo.id()))
                .forEach(photo -> product.addPhoto(ProductPhotoMapper.from(photo, product)));

        this.productJpaRepository.saveAndFlush(product);
    }
}
