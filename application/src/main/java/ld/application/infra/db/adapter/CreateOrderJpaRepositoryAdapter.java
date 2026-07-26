package ld.application.infra.db.adapter;

import ld.application.infra.db.jpa.OrderJpaRepository;
import ld.application.infra.db.mapper.OrderMapper;
import ld.domain.features.order.CreateOrderRepository;
import ld.domain.features.order.model.OrderSnapshot;
import org.springframework.stereotype.Repository;

@Repository
public class CreateOrderJpaRepositoryAdapter implements CreateOrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    public CreateOrderJpaRepositoryAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public void create(OrderSnapshot order) {
        this.orderJpaRepository.saveAndFlush(OrderMapper.from(order));
    }
}
