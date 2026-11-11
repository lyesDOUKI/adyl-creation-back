package ld.domain.features.order.cancel;

import ld.standard.lib.validation.DomainError;

import java.time.Instant;
import java.util.UUID;

public sealed interface CancelOrderError extends DomainError {

    record OrderNotFound(UUID orderId) implements CancelOrderError {
    }

    record OrderAlreadyDelivered(Instant deliveredAt) implements CancelOrderError {}

    record OrderAlreadyAccepted(Instant acceptedAt) implements CancelOrderError {}

    record OrderAlreadyRejected(Instant rejectedAt, String reason) implements CancelOrderError {}
}
