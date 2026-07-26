package ld.domain.features.order;

import ld.domain.features.order.model.OrderSnapshot;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface CreateOrderRepository {
    Set<UUID> findExistingProductIds(Collection<UUID> productsId);
    void create(OrderSnapshot order);
}
