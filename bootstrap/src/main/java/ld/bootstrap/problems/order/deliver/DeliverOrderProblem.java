package ld.bootstrap.problems.order.deliver;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.deliver.DeliverOrderError;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;

import java.time.Instant;
import java.util.UUID;

import static ld.bootstrap.problems.order.deliver.DeliverOrderErrorCode.*;

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

    record AlreadyCancelled(
            @Schema(description = "Date d'annulation de la commande")
            Instant cancelledAt
    ) implements DeliverOrderProblem {

        @Override
        public ApiErrorCode getCode() {
            return ORDER_ALREADY_CANCELLED;
        }

        @Override
        public String getDetail() {
            return "L'opération est impossible sur une commande annulée.";
        }
    }

    static DeliverOrderProblem from(DeliverOrderError error) {
        return switch (error) {
            case DeliverOrderError.OrderAlreadyRejected _ -> new Rejected();
            case DeliverOrderError.OrderNotFound e -> new NotFound(e.orderId());
            case DeliverOrderError.OrderStillPending _ -> new Pending();
            case DeliverOrderError.OrderAlreadyCancelled orderAlreadyCancelled -> new AlreadyCancelled(orderAlreadyCancelled.cancelledAt());
        };
    }
}