package ld.domain.features.order;

import ld.domain.features.order.model.OrderSnapshot;

public interface CreateOrderRepository {
    void create(OrderSnapshot order);
}
