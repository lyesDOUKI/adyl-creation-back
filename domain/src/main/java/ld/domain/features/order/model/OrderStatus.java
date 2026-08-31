package ld.domain.features.order.model;


import ld.domain.valueObjects.Percentage;

import java.time.Instant;

public sealed interface OrderStatus {

    String type();

    enum State implements OrderStatus {
        PENDING, REJECTED, DELIVERED;

        @Override
        public String type() {
            return this.name();
        }
    }

    OrderStatus PENDING = State.PENDING;
    OrderStatus REJECTED = State.REJECTED;
    OrderStatus DELIVERED = State.DELIVERED;

    record Accepted(Instant acceptedAt, Percentage discountApplied) implements OrderStatus {
        @Override
        public String type() {
            return "ACCEPTED";
        }
    }
}