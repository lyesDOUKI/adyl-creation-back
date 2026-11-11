package ld.bootstrap.problems.order.accept;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.accept.AcceptOrderError;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;

import java.time.Instant;
import java.util.UUID;

import static ld.bootstrap.problems.order.accept.AcceptOrderErrorCode.*;

public sealed interface AcceptOrderProblem extends ApiProblem {

    record Rejected() implements AcceptOrderProblem {
        @Override public AcceptOrderErrorCode getCode() { return ORDER_ALREADY_REJECTED; }
        @Override public String getDetail() { return "La commande a été rejetée, elle ne peut plus être acceptée."; }
    }

    record Delivered() implements AcceptOrderProblem {
        @Override public AcceptOrderErrorCode getCode() { return ORDER_ALREADY_DELIVERED; }
        @Override public String getDetail() { return "La commande a déjà été livrée, elle ne peut plus être acceptée."; }
    }

    record NotFound(
            @Schema(description = "Identifiant de la commande introuvable") UUID orderId
    ) implements AcceptOrderProblem {
        @Override public AcceptOrderErrorCode getCode() { return ORDER_NOT_FOUND; }
        @Override public String getDetail() { return "La commande est introuvable."; }
    }

    record AlreadyCancelled(
            @Schema(description = "Date d'annulation de la commande")
            Instant cancelledAt
    ) implements AcceptOrderProblem {

        @Override
        public ApiErrorCode getCode() {
            return ORDER_ALREADY_CANCELLED;
        }

        @Override
        public String getDetail() {
            return "L'opération est impossible sur une commande annulée.";
        }
    }
    static AcceptOrderProblem from(AcceptOrderError error) {
        return switch (error) {
            case AcceptOrderError.OrderAlreadyRejected _ -> new Rejected();
            case AcceptOrderError.OrderAlreadyDelivered _ -> new Delivered();
            case AcceptOrderError.OrderNotFound e -> new NotFound(e.orderId());
            case AcceptOrderError.OrderAlreadyCancelled cancelled -> new AlreadyCancelled(cancelled.cancelledAt());
        };
    }
}