package ld.domain.features.order.model;

import java.util.UUID;

public record OrderAccepted(UUID orderId) implements OrderEvent {
}
