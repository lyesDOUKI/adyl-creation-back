package ld.domain.features.order.model;

import ld.domain.valueObjects.Percentage;

import java.time.Instant;

public sealed interface OrderStatus {

    sealed interface Rejectable extends OrderStatus {}
    sealed interface Cancellable extends OrderStatus {}
    OrderState type();

    record Pending() implements Rejectable, Cancellable {
        @Override
        public OrderState type() {
            return OrderState.PENDING;
        }
    }

    record Accepted(Instant acceptedAt, Percentage discountApplied) implements Rejectable {
        @Override
        public OrderState type() {
            return OrderState.ACCEPTED;
        }
    }
    record Rejected(String reason, Instant rejectedAt) implements OrderStatus {
        @Override
        public OrderState type() {
            return OrderState.REJECTED;
        }

    }
    record Delivered(String observation, DeliveryMethod deliveryMethod, Instant deliveredAt) implements OrderStatus {
        @Override
        public OrderState type() {
            return OrderState.DELIVERED;
        }

    }

    record Cancelled(Instant cancelledAt) implements OrderStatus {

        @Override
        public OrderState type() {
            return OrderState.CANCELLED;
        }
    }

    OrderStatus PENDING = new Pending();

    default boolean canBeCancelled() {
        return this instanceof Cancellable;
    }
}