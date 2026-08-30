package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.OrderSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryOrderLifecycleRepository implements OrderLifecycleRepository {

    private final Map<UUID, OrderSnapshot> store = new HashMap<>();

    @Override
    public Optional<OrderSnapshot> findById(UUID orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public void save(OrderSnapshot orderSnapshot) {
        store.put(orderSnapshot.orderId(), orderSnapshot);
    }
}
