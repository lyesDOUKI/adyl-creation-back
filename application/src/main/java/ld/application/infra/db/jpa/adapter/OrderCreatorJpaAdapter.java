package ld.application.infra.db.jpa.adapter;

import ld.application.infra.db.jpa.OrderJpaRepository;
import ld.application.infra.db.jpa.mapper.OrderMapper;
import ld.domain.features.order.OrderCreator;
import ld.domain.features.order.model.OrderSnapshot;
import org.springframework.stereotype.Repository;

@Repository
public class OrderCreatorJpaAdapter implements OrderCreator {

    private final OrderJpaRepository orderJpaRepository;
    public OrderCreatorJpaAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public void create(OrderSnapshot order) {
        this.orderJpaRepository.saveAndFlush(OrderMapper.from(order));
    }
}
