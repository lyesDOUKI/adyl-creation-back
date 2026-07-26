package ld.domain.features.order.model;

import java.util.UUID;

public record OrderCreated(UUID orderId) implements OrderEvent {
}
