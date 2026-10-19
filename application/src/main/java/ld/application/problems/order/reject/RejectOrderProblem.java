package ld.application.problems.order.reject;

import ld.application.problems.order.OrderProblem;
import ld.application.problems.order.OrderErrorCode;
import ld.spring.web.lib.ApiProblem;
import ld.spring.web.lib.ConflictProblem;
import ld.spring.web.lib.NotFoundProblem;

import java.util.Map;
import java.util.UUID;

public sealed interface RejectOrderProblem extends ApiProblem {

    @OrderProblem(OrderErrorCode.ORDER_ALREADY_DELIVERED)
    record orderAlreadyDelivered() implements RejectOrderProblem, ConflictProblem {

        public String title() {
            return "Commande livrée";
        }

        public String detail() {
            return "L'opération est impossible sur une commande déjà livrée";
        }

    }

    @OrderProblem(OrderErrorCode.ORDER_NOT_FOUND)
    record orderNotFound(UUID orderId) implements RejectOrderProblem, NotFoundProblem {

        public String title() {
            return "Commande introuvable";
        }

        public String detail() {
            return "La commande %s est introuvable".formatted(orderId);
        }

        public Map<String, ?> properties() {
            return Map.of("orderId", orderId);
        }
    }
}
