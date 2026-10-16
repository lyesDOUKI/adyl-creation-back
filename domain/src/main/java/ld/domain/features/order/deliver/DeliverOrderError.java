package ld.domain.features.order.deliver;

import ld.domain.features.order.validation.OrderErrorCode;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.FailureDetail;

import java.util.UUID;

public sealed interface DeliverOrderError extends DomainError {

    record OrderStillPending() implements DeliverOrderError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    OrderErrorCode.PENDING_ORDER,
                    "Commande en attente",
                    "Impossible de livrer une commande en attente");
        }
    }

    record OrderAlreadyRejected() implements DeliverOrderError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    OrderErrorCode.ORDER_HAS_BEEN_REJECTED,
                    "Commande rejetée",
                    "L'opération est impossible sur une commande rejetée");
        }
    }

    record OrderNotFound(UUID orderId) implements DeliverOrderError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.resourceNotFound(
                    OrderErrorCode.ORDER_NOT_FOUND,
                    "Commande introuvable",
                    "La commande %s est introuvable".formatted(orderId));
        }
    }
}
