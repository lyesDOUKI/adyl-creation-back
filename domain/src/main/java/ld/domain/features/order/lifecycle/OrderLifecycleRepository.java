package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.OrderSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface OrderLifecycleRepository {
    void save(OrderSnapshot orderSnapshot);
    Optional<OrderSnapshot> findById(UUID orderId);
}
