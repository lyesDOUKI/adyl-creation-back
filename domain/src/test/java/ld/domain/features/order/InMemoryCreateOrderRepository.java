package ld.domain.features.order;

import ld.domain.features.order.model.Order;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryCreateOrderRepository implements CreateOrderRepository {

    private final Set<UUID> products = new HashSet<>();
    private final Set<Order> orders = new HashSet<>();
    @Override
    public Set<UUID> findExistingProducts(Collection<UUID> productsId) {
        return productsId.stream()
                .filter(this.products::contains)
                .collect(Collectors.toSet());
    }

    @Override
    public void create(Order order) {
        this.orders.add(order);
    }

    public void addProduct(UUID productId) {
        this.products.add(productId);
    }

    public void clearOrders() {
        this.orders.clear();
    }

    public int countOrders() {
        return this.orders.size();
    }

    public Order findCreatedOrder() {
        return this.orders.stream()
                .findFirst()
                .orElseThrow();
    }
}
