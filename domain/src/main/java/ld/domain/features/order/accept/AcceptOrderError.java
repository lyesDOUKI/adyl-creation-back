package ld.domain.features.order.accept;

import ld.domain.features.order.validation.OrderErrorCode;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.FailureDetail;

import java.util.UUID;

public sealed interface AcceptOrderError extends DomainError {

    record OrderAlreadyRejected() implements AcceptOrderError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    OrderErrorCode.ORDER_HAS_BEEN_REJECTED,
                    "Commande rejetée",
                    "L'opération est impossible sur une commande rejetée");
        }
    }

    record OrderAlreadyDelivered() implements AcceptOrderError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    OrderErrorCode.ORDER_HAS_BEEN_DELIVERED,
                    "Commande livrée",
                    "L'opération est impossible sur une commande déjà livrée");
        }
    }

    record OrderNotFound(UUID orderId) implements AcceptOrderError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.resourceNotFound(
                    OrderErrorCode.ORDER_NOT_FOUND,
                    "Commande introuvable",
                    "La commande %s est introuvable".formatted(orderId));
        }
    }
}
