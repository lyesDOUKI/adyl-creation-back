package ld.application.infra.db.jpa.adapter;

import ld.application.infra.db.entity.ProductEntity;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.jpa.mapper.ProductMapper;
import ld.domain.features.product.ProductFinder;
import ld.domain.features.product.model.ProductSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class ProductJpaFinderAdapter implements ProductFinder {

    private final ProductJpaRepository productJpaRepository;

    public ProductJpaFinderAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public List<ProductSnapshot> findAllBy(Collection<UUID> productsId) {
        if (productsId == null || productsId.isEmpty()) {
            return List.of();
        }

        List<ProductEntity> productEntities = this.productJpaRepository.findAllWithColorsAndPhotosByIdIn(productsId);

        return productEntities.stream()
                .map(ProductMapper::toSnapshot)
                .toList();
    }
}