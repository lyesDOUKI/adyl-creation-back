package ld.domain.features.order.model;

import ld.domain.valueObjects.Price;
import ld.lib.AggregateRoot;
import ld.lib.Snapshottable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<UUID, OrderEvent> implements Snapshottable<OrderSnapshot> {

    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String address;
    private final String city;
    private final String message;
    private Price total;
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(String name, String email, String phoneNumber, String address, String city, String message) {
        this.setId(UUID.randomUUID());
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.city = city;
        this.message = message;
    }

    public static Order create(String name, String email, String phoneNumber, String address, String city, String message) {
        return new Order(name, email, phoneNumber, address, city, message);
    }

    public void calculateOrder(List<OrderItem> orderItems) {
        orderItems.forEach(OrderItem::calculateTotal);
        this.total = orderItems.stream()
                .map(OrderItem::getTotal)
                .reduce(Price.zero(), Price::add);
        this.orderItems = orderItems;
    }

    @Override
    public OrderSnapshot toSnapshot() {
        return new OrderSnapshot(
                this.getId(),
                this.name,
                this.email,
                this.phoneNumber,
                this.address,
                this.city,
                this.message,
                this.total.value(),
                this.orderItems.stream()
                        .map(this::toItemSnapshot)
                        .toList()
        );
    }

    private OrderSnapshot.OrderItemSnapshot toItemSnapshot(OrderItem item) {
        return new OrderSnapshot.OrderItemSnapshot(
                item.getItemId(),
                item.getProductId(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTotalValue(),
                item.getColor()
        );
    }
}
