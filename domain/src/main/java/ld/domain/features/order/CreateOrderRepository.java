package ld.domain.features.order;

import ld.domain.features.order.model.Order;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface CreateOrderRepository {
    Set<UUID> findExistingProducts(Collection<UUID> productsId);
    void create(Order order);
}
