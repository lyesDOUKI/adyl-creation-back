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

    private final CustomerInfo customerInfo;
    private final String message;
    private Price total;
    private OrderStatus orderStatus;
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(CustomerInfo customerInfo, String message) {
        this.setId(UUID.randomUUID());
        this.customerInfo = customerInfo;
        this.message = message;
        this.orderStatus = OrderStatus.PENDING;
        this.addDomainEvent(new OrderCreated(getId()));
    }

    public Order(UUID orderId, String message, CustomerInfo customerInfo, BigDecimal total, OrderStatus orderStatus, List<OrderItem> orderItems) {
        setId(orderId);
        this.message = message;
        this.customerInfo = customerInfo;
        this.total = new Price(total);
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
    }

    public static Order create(CustomerInfo customerInfo, String message) {
        return new Order(customerInfo, message);
    }

    public static Order from(OrderSnapshot snapshot) {
        return new Order(
                snapshot.orderId(),
                snapshot.message(),
                snapshot.customerInfo(),
                snapshot.total(),
                snapshot.orderStatus(),
                snapshot.items().stream().map(OrderItem::from).toList()
        );
    }

    public void calculateOrder(List<OrderItem> orderItems) {
        orderItems.forEach(OrderItem::calculateTotal);
        this.orderItems = orderItems;
        this.total = this.calculateTotal();
    }

    private Price calculateTotal() {
        return this.orderItems.stream()
                .map(OrderItem::getTotal)
                .reduce(Price.zero(), Price::add);
    }

    public Result<Order> accept(AppliedDiscount discount, Instant acceptedAt) {
        return switch (this.orderStatus) {
            case OrderStatus.Accepted _ -> Result.success(this);

            case OrderStatus.Pending _ -> {
                var appliedRate = switch (discount) {
                    case AppliedDiscount.Claimed(Percentage rate) -> {
                        applyDiscountToItems(rate);
                        yield rate;
                    }
                    case AppliedDiscount.None _ -> Percentage.ZERO;
                };
                this.total = this.calculateTotal();
                this.orderStatus = new OrderStatus.Accepted(acceptedAt, appliedRate);

                addDomainEvent(new OrderAccepted(getId(), this.total.value()));
                yield Result.success(this);
            }

            case OrderStatus.Rejected _ -> Result.businessFailure(
                    OrderErrorCode.ORDER_HAS_BEEN_REJECTED,
                    "Commande rejetée", "Impossible d'accepter une commande rejetée");

            case OrderStatus.Delivered _ -> Result.businessFailure(
                    OrderErrorCode.ORDER_HAS_BEEN_DELIVERED,
                    "Commande livrée", "Impossible d'accepter une commande déjà livrée");
        };
    }
    public Result<Order> reject(String reason, Instant rejectedAt) {
        return switch (this.orderStatus) {
            case OrderStatus.Rejectable _ -> {
                this.orderStatus = new OrderStatus.Rejected(reason, rejectedAt);
                addDomainEvent(new OrderRejected(getId(), reason));
                yield Result.success(this);
            }

            case OrderStatus.Delivered _ -> Result.businessFailure(
                    OrderErrorCode.ORDER_HAS_BEEN_DELIVERED,
                    "Commande livrée",
                    "Impossible de rejeter une commande déjà livrée"
            );

            case OrderStatus.Rejected _ -> Result.success(this);
        };
    }
    public Result<Order> deliver(String observation, DeliveryMethod deliveryMethod, Instant deliveredAt) {
        return switch (this.orderStatus) {
            case OrderStatus.Accepted _ -> {
                this.orderStatus = new OrderStatus.Delivered(observation, deliveryMethod, deliveredAt);
                addDomainEvent(new OrderDelivered(getId(), deliveryMethod));
                yield Result.success(this);
            }
            case OrderStatus.Delivered _ -> Result.success(this);
            case OrderStatus.Pending _ -> Result.businessFailure(OrderErrorCode.PENDING_ORDER,
                    "Commande en attente", "Impossible de livrer une commande en attente");
            case OrderStatus.Rejected _ -> Result.businessFailure(OrderErrorCode.ORDER_HAS_BEEN_REJECTED,
                    "Commande rejetée", "Impossible de livrer une commande rejetée");
        };
    }

    private void applyDiscountToItems(Percentage discount) {
        Price runningOriginalTotal = Price.zero();
        Price runningDiscount = Price.zero();

        for (OrderItem item : this.orderItems) {
            Price currentItemTotal = item.getTotal();

            runningOriginalTotal = runningOriginalTotal.add(currentItemTotal);
            Price expectedTotalDiscount = runningOriginalTotal.percentageOf(discount);
            Price itemDiscount = expectedTotalDiscount.subtract(runningDiscount);

            item.setTotal(currentItemTotal.subtract(itemDiscount));
            item.setDiscountRate(discount);
            runningDiscount = runningDiscount.add(itemDiscount);
        }
    }

    @Override
    public OrderSnapshot toSnapshot() {
        return new OrderSnapshot(
                this.getId(),
                this.customerInfo,
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
                item.getTotal(),
                item.getDiscountRate(),
                item.getColor()
        );
    }

    public UUID customerIdentity() {
        return this.customerInfo.identitySubject();
    }
}
