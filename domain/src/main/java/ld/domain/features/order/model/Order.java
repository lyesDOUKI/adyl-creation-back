package ld.domain.features.order.model;

import ld.domain.features.order.CreateOrderCommand;
import ld.domain.valueObjects.Price;
import ld.standard.lib.AggregateRoot;
import ld.standard.lib.Snapshottable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<UUID, OrderEvent> implements Snapshottable<OrderSnapshot> {

    private final Customer customer;
    private final String message;
    private Price total;
    private final OrderStatus orderStatus;
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(Customer customer, String message) {
        this.setId(UUID.randomUUID());
        this.customer = customer;
        this.message = message;
        this.orderStatus = OrderStatus.PENDING;
    }

    public static Order create(Customer customer, String message) {
        return new Order(customer, message);
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
                this.customer,
                this.message,
                this.total.value(),
                this.orderStatus,
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
