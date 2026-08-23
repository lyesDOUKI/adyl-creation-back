package ld.application.infra.db.adapter;

import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.mapper.ProductMapper;
import ld.domain.features.product.CreateProductRepository;
import ld.domain.features.product.model.ProductSnapshot;
import org.springframework.stereotype.Repository;

@Repository
public class CreateProductJpaRepositoryAdapter implements CreateProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public CreateProductJpaRepositoryAdapter(ProductJpaRepository productJpaRepository) {
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
