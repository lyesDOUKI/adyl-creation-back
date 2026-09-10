package ld.domain.features.shared;

import ld.domain.features.order.model.CustomerInfo;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OrderSnapshotTestBuilder {

    private UUID orderId = UUID.randomUUID();
    private CustomerInfo customerInfo = defaultCustomer();
    private String message = "no message";
    private BigDecimal total = BigDecimal.TEN;
    private OrderStatus orderStatus = new OrderStatus.Pending();
    private List<OrderSnapshot.OrderItemSnapshot> items = List.of();

    public static OrderSnapshotTestBuilder anOrder() {
        return new OrderSnapshotTestBuilder();
    }

    private static CustomerInfo defaultCustomer() {
        return new CustomerInfo(UUID.randomUUID(), new CustomerInfo.DeliveryAddress("7 rue test", "avignon"));
    }

    public OrderSnapshotTestBuilder withOrderId(UUID orderId) {
        this.orderId = orderId;
        return this;
    }

    public OrderSnapshotTestBuilder withCustomer(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
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
        return new OrderSnapshot(orderId, customerInfo, message, total, orderStatus, items);
    }
}
