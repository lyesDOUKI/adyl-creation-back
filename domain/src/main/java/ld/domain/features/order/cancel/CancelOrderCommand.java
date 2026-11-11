package ld.domain.features.order.cancel;

import java.util.UUID;

public record CancelOrderCommand(UUID orderId) {
}
