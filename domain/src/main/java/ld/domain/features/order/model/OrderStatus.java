package ld.domain.features.order.model;

import ld.domain.valueObjects.Percentage;

import java.time.Instant;

public sealed interface OrderStatus {

    OrderState type();

    record Pending() implements OrderStatus {
        @Override
        public OrderState type() {
            return OrderState.PENDING;
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

    record Accepted(Instant acceptedAt, Percentage discountApplied) implements OrderStatus {
        @Override
        public OrderState type() {
            return OrderState.ACCEPTED;
        }
    }

    OrderStatus PENDING = new Pending();
}