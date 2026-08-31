package ld.application.request;

import ld.domain.features.order.reject.RejectOrderCommand;

import java.util.UUID;

public record RejectOrderRequest(
        String reason
) {
    public RejectOrderCommand to(UUID orderId) {
        return new RejectOrderCommand(orderId, reason);
    }
}
