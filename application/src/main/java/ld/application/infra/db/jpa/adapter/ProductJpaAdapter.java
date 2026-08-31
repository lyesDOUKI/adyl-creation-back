package ld.application.infra.db.jpa.adapter;

import ld.application.infra.db.entity.ProductEntity;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.jpa.mapper.ProductMapper;
import ld.domain.features.product.ProductChecker;
import ld.domain.features.product.ProductCreator;
import ld.domain.features.product.lifecycle.ProductEditor;
import ld.domain.features.product.model.ProductSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductJpaAdapter implements ProductCreator, ProductChecker, ProductEditor {

    private final ProductJpaRepository productJpaRepository;

    public ProductJpaAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public boolean alreadyExists(String name) {
        return productJpaRepository.existsByName(name);
    }

    @Override
    public void create(ProductSnapshot snapshot) {
        productJpaRepository.saveAndFlush(
                ProductMapper.toEntity(snapshot)
        );
    }

    @Override
    public Optional<ProductSnapshot> findById(UUID productId) {
        return productJpaRepository.findById(productId)
                .map(ProductMapper::toSnapshot);
    }

    @Override
    public void save(ProductSnapshot snapshot) {

        ProductEntity productEntityEntity = productJpaRepository.findById(snapshot.productId())
                .orElseThrow(() -> new IllegalStateException(
                        String.format(
                                "Le produit %s doit exister pour persister sa mise à jour",
                                snapshot.productId()
                        )
                ));

        ProductMapper.updateEntity(snapshot, productEntityEntity);

        productJpaRepository.saveAndFlush(productEntityEntity);
    }
}