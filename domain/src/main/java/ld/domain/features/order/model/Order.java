package ld.domain.features.order.model;

import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.valueObjects.Price;
import ld.standard.lib.AggregateRoot;
import ld.standard.lib.Snapshottable;
import ld.standard.lib.validation.Result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<UUID, OrderEvent> implements Snapshottable<OrderSnapshot> {

    private final Customer customer;
    private final String message;
    private Price total;
    private OrderStatus orderStatus;
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(Customer customer, String message) {
        this.setId(UUID.randomUUID());
        this.customer = customer;
        this.message = message;
        this.orderStatus = OrderStatus.PENDING;
        this.addDomainEvent(new OrderCreated(getId()));
    }

    public Order(UUID orderId, String message, Customer customer, BigDecimal total, OrderStatus orderStatus) {
        setId(orderId);
        this.message = message;
        this.customer = customer;
        this.total = new Price(total);
        this.orderStatus = orderStatus;
    }

    public static Order create(Customer customer, String message) {
        return new Order(customer, message);
    }

    public static Order from(OrderSnapshot snapshot) {
        return new Order(
                snapshot.orderId(),
                snapshot.message(),
                snapshot.customer(),
                snapshot.total(),
                snapshot.orderStatus()
        );
    }

    public void calculateOrder(List<OrderItem> orderItems) {
        orderItems.forEach(OrderItem::calculateTotal);
        this.total = orderItems.stream()
                .map(OrderItem::getTotal)
                .reduce(Price.zero(), Price::add);
        this.orderItems = orderItems;
    }

    public Result<Order> accept() {
        if (this.orderStatus == OrderStatus.DELIVERED) {
            return Result.businessFailure(OrderErrorCode.ORDER_HAS_BEEN_DELIVERED, "Commande livré",
                    "Impossible d'accepter cette commande car elle est déjà livré");
        }
        if (this.orderStatus == OrderStatus.REJECTED) {
            return Result.businessFailure(OrderErrorCode.ORDER_HAS_BEEN_REJECTED, "Commande rejeté",
                    "Impossible d'accepter cette commande car elle est rejeté");
        }
        orderStatus = OrderStatus.ACCEPTED;
        addDomainEvent(new OrderAccepted(getId()));
        return Result.success(this);
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
