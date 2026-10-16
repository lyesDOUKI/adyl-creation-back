package ld.domain.features.order.reject;

import ld.domain.features.order.validation.OrderErrorCode;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.FailureDetail;

import java.util.UUID;

public sealed interface RejectOrderError extends DomainError {

    record OrderAlreadyDelivered() implements RejectOrderError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    OrderErrorCode.ORDER_HAS_BEEN_DELIVERED,
                    "Commande livrée",
                    "L'opération est impossible sur une commande déjà livrée");
        }
    }

    record OrderNotFound(UUID orderId) implements RejectOrderError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.resourceNotFound(
                    OrderErrorCode.ORDER_NOT_FOUND,
                    "Commande introuvable",
                    "La commande %s est introuvable".formatted(orderId));
        }
    }
}
