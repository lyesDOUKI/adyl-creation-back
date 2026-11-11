package ld.bootstrap.problems.order.cancel;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.cancel.CancelOrderError;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;

import java.util.UUID;

import static ld.bootstrap.problems.order.cancel.CancelOrderErrorCode.*;

public sealed interface CancelOrderProblem extends ApiProblem {

    record Accepted() implements CancelOrderProblem {

        @Override
        public ApiErrorCode getCode() {
            return ACCEPTED;
        }

        @Override
        public String getDetail() {
            return "La commande a été acceptée ; elle ne peut donc pas être annulée.";
        }
    }

    record Delivered() implements CancelOrderProblem {

        @Override
        public ApiErrorCode getCode() {
            return DELIVERED;
        }

        @Override
        public String getDetail() {
            return "La commande a été livrée ; elle ne peut donc pas être annulée.";
        }
    }

    record Rejected() implements CancelOrderProblem {

        @Override
        public ApiErrorCode getCode() {
            return REJECTED;
        }

        @Override
        public String getDetail() {
            return "La commande a été rejetée ; elle ne peut donc pas être annulée.";
        }
    }

    record NotFound(
            @Schema(description = "Identifiant de la commande introuvable")
            UUID orderId) implements CancelOrderProblem {

        @Override
        public ApiErrorCode getCode() {
            return ORDER_NOT_FOUND;
        }

        @Override
        public String getDetail() {
            return "La commande demandée est introuvable";
        }
    }

    static CancelOrderProblem from(CancelOrderError cancelOrderError) {
        return switch (cancelOrderError) {
            case CancelOrderError.OrderAlreadyAccepted _ -> new Accepted();
            case CancelOrderError.OrderAlreadyDelivered _ -> new Delivered();
            case CancelOrderError.OrderAlreadyRejected _ -> new Rejected();
            case CancelOrderError.OrderNotFound orderNotFound -> new NotFound(orderNotFound.orderId());
        };
    }
}
