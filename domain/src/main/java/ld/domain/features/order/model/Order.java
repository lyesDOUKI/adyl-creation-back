package ld.domain.features.order.model;

import ld.domain.valueObjects.Price;
import ld.lib.AggregateRoot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<UUID, OrderEvent> {

    private final String email;
    private final String phoneNumber;
    private final String address;
    private final String city;
    private final String message;
    private Price total;
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(String email, String phoneNumber, String address, String city, String message) {
        this.phoneNumber = phoneNumber;
        this.setId(UUID.randomUUID());
        this.email = email;
        this.address = address;
        this.city = city;
        this.message = message;
    }

    public static Order create(String email, String phoneNumber, String address, String city, String message) {
        return new Order(email, phoneNumber, address, city, message);
    }

    public BigDecimal getTotal() {
        return total.value();
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void calculateOrder(List<OrderItem> orderItems) {
        orderItems.forEach(OrderItem::calculateTotal);
        this.total = orderItems.stream()
                .map(OrderItem::getTotal)
                .reduce(Price.zero(), Price::add);
        this.orderItems = orderItems;
    }
}
