package ld.application.infra.db.adapter;

import ld.application.infra.db.mapper.ProductMapper;
import ld.domain.features.product.ProductChecker;
import ld.domain.features.product.ProductCreator;
import ld.domain.features.product.model.ProductSnapshot;
import org.springframework.stereotype.Repository;

@Repository
public class ProductJpaRepository implements ProductCreator, ProductChecker {

    private final ld.application.infra.db.jpa.ProductJpaRepository productJpaRepository;

    public ProductJpaRepository(ld.application.infra.db.jpa.ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public boolean alreadyExists(String name) {
        return this.productJpaRepository.existsByName(name);
    }

    @Override
    public void create(ProductSnapshot product) {
        this.productJpaRepository.saveAndFlush(ProductMapper.from(product));
    }
}
