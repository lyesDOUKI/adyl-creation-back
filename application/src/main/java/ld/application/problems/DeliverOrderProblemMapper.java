package ld.application.problems;

import ld.domain.features.order.deliver.DeliverOrderError;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.Problems;

import java.util.Map;

public class DeliverOrderProblemMapper implements ProblemMapper<DeliverOrderError> {

    @Override
    public ProblemDetailResponse toProblem(DeliverOrderError error) {
        return switch (error) {

            case DeliverOrderError.OrderStillPending ignored -> Problems.conflict(
                    OrderErrorCode.PENDING_ORDER,
                    "Commande en attente",
                    "Impossible de livrer une commande en attente");

            case DeliverOrderError.OrderAlreadyRejected ignored -> Problems.conflict(
                    OrderErrorCode.ORDER_HAS_BEEN_REJECTED,
                    "Commande rejetée",
                    "L'opération est impossible sur une commande rejetée");

            case DeliverOrderError.OrderNotFound e -> Problems.notFound(
                    OrderErrorCode.ORDER_NOT_FOUND,
                    "Commande introuvable",
                    "La commande %s est introuvable".formatted(e.orderId()),
                    Map.of("orderId", e.orderId()));
        };
    }
}
