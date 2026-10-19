package ld.application.problems;

import ld.domain.features.order.reject.RejectOrderError;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.Problems;

import java.util.Map;

public class RejectOrderProblemMapper implements ProblemMapper<RejectOrderError> {

    @Override
    public ProblemDetailResponse toProblem(RejectOrderError error) {
        return switch (error) {

            case RejectOrderError.OrderAlreadyDelivered ignored -> Problems.conflict(
                    OrderErrorCode.ORDER_HAS_BEEN_DELIVERED,
                    "Commande livrée",
                    "L'opération est impossible sur une commande déjà livrée");

            case RejectOrderError.OrderNotFound e -> Problems.notFound(
                    OrderErrorCode.ORDER_NOT_FOUND,
                    "Commande introuvable",
                    "La commande %s est introuvable".formatted(e.orderId()),
                    Map.of("orderId", e.orderId()));
        };
    }
}
