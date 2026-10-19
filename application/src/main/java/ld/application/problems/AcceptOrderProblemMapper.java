package ld.application.problems;

import ld.domain.features.order.accept.AcceptOrderError;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.Problems;

import java.util.Map;

public class AcceptOrderProblemMapper implements ProblemMapper<AcceptOrderError> {

    @Override
    public ProblemDetailResponse toProblem(AcceptOrderError error) {
        return switch (error) {

            case AcceptOrderError.OrderAlreadyRejected ignored -> Problems.conflict(
                    OrderErrorCode.ORDER_HAS_BEEN_REJECTED,
                    "Commande rejetée",
                    "L'opération est impossible sur une commande rejetée");

            case AcceptOrderError.OrderAlreadyDelivered ignored -> Problems.conflict(
                    OrderErrorCode.ORDER_HAS_BEEN_DELIVERED,
                    "Commande livrée",
                    "L'opération est impossible sur une commande déjà livrée");

            case AcceptOrderError.OrderNotFound e -> Problems.notFound(
                    OrderErrorCode.ORDER_NOT_FOUND,
                    "Commande introuvable",
                    "La commande %s est introuvable".formatted(e.orderId()),
                    Map.of("orderId", e.orderId()));
        };
    }
}
