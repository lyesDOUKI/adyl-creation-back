package ld.domain.features.order;

import ld.domain.features.order.model.OrderSnapshot;

public interface OrderCreator {
    void create(OrderSnapshot order);
}
