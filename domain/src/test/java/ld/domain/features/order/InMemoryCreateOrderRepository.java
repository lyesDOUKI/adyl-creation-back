package ld.domain.features.order;

import ld.domain.features.order.model.OrderSnapshot;

import java.util.HashSet;
import java.util.Set;

public class InMemoryCreateOrderRepository implements CreateOrderRepository {

    private final Set<OrderSnapshot> orders = new HashSet<>();

    @Override
    public void create(OrderSnapshot order) {
        this.orders.add(order);
    }

    public int countOrders() {
        return this.orders.size();
    }

    public OrderSnapshot findCreatedOrder() {
        return this.orders.stream()
                .findFirst()
                .orElseThrow();
    }
}
