package ld.domain.features.order.reject;

import java.util.UUID;

public record RejectOrderCommand (UUID orderId, String reason) {
}
