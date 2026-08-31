package ld.domain.features.order.model;

import java.util.UUID;

public record OrderRejected(UUID orderId, String reason) implements OrderEvent {
}
