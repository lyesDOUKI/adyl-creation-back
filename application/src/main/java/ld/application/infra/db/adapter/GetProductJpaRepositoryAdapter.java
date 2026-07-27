package ld.application.infra.db.adapter;

import ld.application.infra.db.entity.Product;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.domain.features.product.GetProductRepository;
import ld.domain.features.product.model.ProductColor;
import ld.domain.features.product.model.ProductSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class GetProductJpaRepositoryAdapter implements GetProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public GetProductJpaRepositoryAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public List<ProductSnapshot> getAllBy(Collection<UUID> productsId) {
        return productJpaRepository.findAllById(productsId).stream()
                .map(this::toSnapshot)
                .toList();
    }

    private ProductSnapshot toSnapshot(Product entity) {
        return new ProductSnapshot(
                entity.getId(),
                entity.getName(),
                entity.getUnitPrice(),
                entity.getColors().stream()
                        .map(ProductColor::new).toList(),
                entity.getStatus()
        );
    }
}