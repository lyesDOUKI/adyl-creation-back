package ld.domain.features.order.deliver;

import ld.standard.lib.validation.DomainError;

import java.util.UUID;

public sealed interface DeliverOrderError extends DomainError {

    record OrderStillPending() implements DeliverOrderError {
    }

    record OrderAlreadyRejected() implements DeliverOrderError {
    }

    record OrderNotFound(UUID orderId) implements DeliverOrderError {
    }
}
