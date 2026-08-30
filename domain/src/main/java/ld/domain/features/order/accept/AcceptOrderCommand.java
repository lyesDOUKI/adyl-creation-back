package ld.domain.features.order.accept;

import java.util.UUID;

public record AcceptOrderCommand(UUID orderId) {
}
