package ld.domain.features.order.model;

import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.valueObjects.Percentage;
import ld.domain.valueObjects.Price;
import ld.standard.lib.AggregateRoot;
import ld.standard.lib.Snapshottable;
import ld.standard.lib.validation.Result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<UUID, OrderEvent> implements Snapshottable<OrderSnapshot> {

    private static final Percentage FIRST_ORDER_DISCOUNT_RATE = Percentage.of(10);
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

    public Result<Order> accept(boolean isFirstAcceptedOrder, Instant acceptedAt) {
        return switch (this.orderStatus) {

            case OrderStatus.Accepted _ -> Result.success(this);

            case OrderStatus.Pending _ -> {
                Percentage discount = isFirstAcceptedOrder ? FIRST_ORDER_DISCOUNT_RATE : Percentage.of(0);
                if (isFirstAcceptedOrder) {
                    this.total = this.total.subtract(this.total.percentageOf(discount));
                }
                this.orderStatus = new OrderStatus.Accepted(acceptedAt, discount);
                addDomainEvent(new OrderAccepted(getId(), this.total.value()));
                yield Result.success(this);
            }

            case OrderStatus.Rejected _ -> Result.businessFailure(
                    OrderErrorCode.ORDER_HAS_BEEN_REJECTED,
                    "Commande rejetée",
                    "Impossible d'accepter cette commande car elle est rejetée");

            case OrderStatus.Delivered _ -> Result.businessFailure(
                    OrderErrorCode.ORDER_HAS_BEEN_DELIVERED,
                    "Commande livrée",
                    "Impossible d'accepter cette commande car elle est déjà livrée");
        };
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

    public String customerEmail() {
        return this.customer.email();
    }
}
