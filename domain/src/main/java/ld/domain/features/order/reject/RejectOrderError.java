package ld.domain.features.order.reject;

import ld.standard.lib.validation.DomainError;

import java.util.UUID;

public sealed interface RejectOrderError extends DomainError {

    record OrderAlreadyDelivered() implements RejectOrderError {
    }

    record OrderNotFound(UUID orderId) implements RejectOrderError {
    }
}
