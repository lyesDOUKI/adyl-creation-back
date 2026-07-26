package ld.application.infra.db.adapter;

import ld.application.infra.db.jpa.OrderJpaRepository;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.mapper.OrderMapper;
import ld.domain.features.order.CreateOrderRepository;
import ld.domain.features.order.model.OrderSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public class CreateOrderJpaRepositoryAdapter implements CreateOrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final ProductJpaRepository productJpaRepository;
    public CreateOrderJpaRepositoryAdapter(OrderJpaRepository orderJpaRepository,
                                           ProductJpaRepository productJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Set<UUID> findExistingProductIds(Collection<UUID> productsId) {
        return this.productJpaRepository.findExistingIds(productsId);
    }

    @Override
    public void create(OrderSnapshot order) {
        this.orderJpaRepository.saveAndFlush(OrderMapper.from(order));
    }
}
