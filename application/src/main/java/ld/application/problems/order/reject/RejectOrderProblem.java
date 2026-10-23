package ld.application.problems.order.reject;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.reject.RejectOrderError;
import ld.spring.web.lib.ApiProblem;

import java.util.UUID;

import static ld.application.problems.order.reject.RejectOrderErrorCode.ORDER_ALREADY_DELIVERED;
import static ld.application.problems.order.reject.RejectOrderErrorCode.ORDER_NOT_FOUND;

public sealed interface RejectOrderProblem extends ApiProblem {

    record AlreadyDelivered() implements RejectOrderProblem {
        @Override public RejectOrderErrorCode getCode() { return ORDER_ALREADY_DELIVERED; }
        @Override public String getDetail() { return "L'opération est impossible sur une commande déjà livrée."; }
    }

    record NotFound(
            @Schema(description = "Identifiant de la commande introuvable") UUID orderId
    ) implements RejectOrderProblem {
        @Override public RejectOrderErrorCode getCode() { return ORDER_NOT_FOUND; }
        @Override public String getDetail() { return "La commande est introuvable."; }
    }

    static RejectOrderProblem from(RejectOrderError error) {
        return switch (error) {
            case RejectOrderError.OrderAlreadyDelivered _ -> new AlreadyDelivered();
            case RejectOrderError.OrderNotFound e -> new NotFound(e.orderId());
        };
    }
}