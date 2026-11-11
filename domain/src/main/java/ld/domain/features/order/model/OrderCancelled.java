package ld.domain.features.order.model;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelled(UUID orderId, Instant cancelledAt) implements OrderEvent {
}
