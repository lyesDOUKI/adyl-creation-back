package ld.domain.features.order.model;

import java.util.UUID;

public record OrderDelivered(UUID orderId, DeliveryMethod deliveryMethod) implements OrderEvent {
}
