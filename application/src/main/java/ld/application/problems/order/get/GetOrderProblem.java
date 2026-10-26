package ld.application.problems.order.get;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.ApiProblem;

import java.util.UUID;

import static ld.application.problems.order.get.GetOrderErrorCode.ORDER_NOT_FOUND;


public sealed interface GetOrderProblem extends ApiProblem {

    record OrderNotFound(
            @Schema(description = "Identifiant de la commande introuvable") UUID orderId
    ) implements GetOrderProblem {
        @Override public GetOrderErrorCode getCode() { return ORDER_NOT_FOUND; }
        @Override public String getDetail() { return "La commande est introuvable."; }
    }
}