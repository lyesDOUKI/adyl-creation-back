package ld.application.problems.order.deliver;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.deliver.DeliverOrderError;
import ld.spring.web.lib.ApiProblem;

import java.util.UUID;

import static ld.application.problems.order.deliver.DeliverOrderErrorCode.ORDER_ALREADY_REJECTED;
import static ld.application.problems.order.deliver.DeliverOrderErrorCode.ORDER_NOT_FOUND;
import static ld.application.problems.order.deliver.DeliverOrderErrorCode.ORDER_STILL_PENDING;

public sealed interface DeliverOrderProblem extends ApiProblem {

    record Pending() implements DeliverOrderProblem {
        @Override public DeliverOrderErrorCode getCode() { return ORDER_STILL_PENDING; }
        @Override public String getDetail() { return "Impossible de livrer une commande en attente."; }
    }

    record Rejected() implements DeliverOrderProblem {
        @Override public DeliverOrderErrorCode getCode() { return ORDER_ALREADY_REJECTED; }
        @Override public String getDetail() { return "La commande a été rejetée, elle ne peut plus être livrée."; }
    }

    record NotFound(
            @Schema(description = "Identifiant de la commande introuvable") UUID orderId
    ) implements DeliverOrderProblem {
        @Override public DeliverOrderErrorCode getCode() { return ORDER_NOT_FOUND; }
        @Override public String getDetail() { return "La commande est introuvable."; }
    }

    static DeliverOrderProblem from(DeliverOrderError error) {
        return switch (error) {
            case DeliverOrderError.OrderAlreadyRejected _ -> new Rejected();
            case DeliverOrderError.OrderNotFound e -> new NotFound(e.orderId());
            case DeliverOrderError.OrderStillPending _ -> new Pending();
        };
    }
}