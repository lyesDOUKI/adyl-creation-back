package ld.domain.features.order.accept;

import ld.standard.lib.validation.DomainError;

import java.util.UUID;

public sealed interface AcceptOrderError extends DomainError {

    record OrderAlreadyRejected() implements AcceptOrderError {
    }

    record OrderAlreadyDelivered() implements AcceptOrderError {
    }

    record OrderNotFound(UUID orderId) implements AcceptOrderError {
    }
}
