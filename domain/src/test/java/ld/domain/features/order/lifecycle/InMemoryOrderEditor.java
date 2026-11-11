package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.OrderSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryOrderEditor implements OrderEditor {

    private final Map<UUID, OrderSnapshot> store = new HashMap<>();
    private int pinged = 0;
    @Override
    public Optional<OrderSnapshot> findById(UUID orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public void save(OrderSnapshot orderSnapshot) {
        pinged++;
        store.put(orderSnapshot.orderId(), orderSnapshot);
    }

    public int attempt() {
        return pinged;
    }

    public void reset() {
        pinged = 0;
    }
}
