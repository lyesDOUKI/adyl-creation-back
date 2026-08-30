package ld.domain.features.order.model;


import ld.domain.valueObjects.Percentage;

import java.time.Instant;

public sealed interface OrderStatus {
    enum State implements OrderStatus {
        PENDING, REJECTED, DELIVERED
    }

    OrderStatus PENDING = State.PENDING;
    OrderStatus REJECTED = State.REJECTED;
    OrderStatus DELIVERED = State.DELIVERED;

    record Accepted(Instant acceptedAt, Percentage discountApplied) implements OrderStatus {}
}