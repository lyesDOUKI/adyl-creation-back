package ld.domain.features.order.model;


import ld.domain.valueObjects.Percentage;

import java.time.Instant;

public sealed interface OrderStatus {

    Pending PENDING = new Pending();
    Rejected REJECTED = new Rejected();
    Delivered DELIVERED = new Delivered();

    record Pending() implements OrderStatus {}

    record Accepted(Instant acceptedAt, Percentage discountApplied) implements OrderStatus {}

    record Rejected() implements OrderStatus {}

    record Delivered() implements OrderStatus {}
}