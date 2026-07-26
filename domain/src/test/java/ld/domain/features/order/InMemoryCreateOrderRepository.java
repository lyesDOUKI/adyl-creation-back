package ld.domain.features.order;

import ld.domain.features.order.model.OrderSnapshot;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryCreateOrderRepository implements CreateOrderRepository {

    private final Set<UUID> products = new HashSet<>();
    private final Set<OrderSnapshot> orders = new HashSet<>();
    @Override
    public Set<UUID> findExistingProducts(Collection<UUID> productsId) {
        return productsId.stream()
                .filter(this.products::contains)
                .collect(Collectors.toSet());
    }

    @Override
    public void create(OrderSnapshot order) {
        this.orders.add(order);
    }

    public void addProduct(UUID productId) {
        this.products.add(productId);
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
