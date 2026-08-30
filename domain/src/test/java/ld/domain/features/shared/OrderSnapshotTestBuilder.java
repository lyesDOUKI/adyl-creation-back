package ld.domain.features.shared;

import ld.domain.features.order.model.Customer;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OrderSnapshotTestBuilder {

    private UUID orderId = UUID.randomUUID();
    private Customer customer = defaultCustomer();
    private String message = "no message";
    private BigDecimal total = BigDecimal.TEN;
    private OrderStatus orderStatus = OrderStatus.PENDING;
    private List<OrderSnapshot.OrderItemSnapshot> items = List.of();

    public static OrderSnapshotTestBuilder anOrder() {
        return new OrderSnapshotTestBuilder();
    }

    private static Customer defaultCustomer() {
        return new Customer("test", "test@test.com", "0123456789", "7 rue test", "avignon");
    }

    public OrderSnapshotTestBuilder withOrderId(UUID orderId) {
        this.orderId = orderId;
        return this;
    }

    public OrderSnapshotTestBuilder withCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public OrderSnapshotTestBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public OrderSnapshotTestBuilder withTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public OrderSnapshotTestBuilder withOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
        return this;
    }

    public OrderSnapshotTestBuilder withItems(List<OrderSnapshot.OrderItemSnapshot> items) {
        this.items = items;
        return this;
    }

    public OrderSnapshot build() {
        return new OrderSnapshot(orderId, customer, message, total, orderStatus, items);
    }
}
