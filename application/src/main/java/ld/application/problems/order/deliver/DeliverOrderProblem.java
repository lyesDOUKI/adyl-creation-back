package ld.application.problems.order.deliver;

import ld.application.problems.order.OrderProblem;
import ld.application.problems.order.OrderErrorCode;
import ld.spring.web.lib.ApiProblem;
import ld.spring.web.lib.ConflictProblem;
import ld.spring.web.lib.NotFoundProblem;
import ld.spring.web.lib.UnprocessableProblem;

import java.util.Map;
import java.util.UUID;

public sealed interface DeliverOrderProblem extends ApiProblem {

    @OrderProblem(OrderErrorCode.ORDER_STILL_PENDING)
    record pending() implements DeliverOrderProblem, UnprocessableProblem {
        @Override
        public String title() {
            return "Commande en attente";
        }
        @Override
        public String detail() {
            return "Impossible de livrer une commande en attente";
        }
    }

    @OrderProblem(OrderErrorCode.ORDER_ALREADY_REJECTED)
    record Rejected() implements DeliverOrderProblem, ConflictProblem {
        public String title()  { return "Commande rejetée"; }
        public String detail() { return "La commande a été rejetée, elle ne peut plus être acceptée."; }
    }

    @OrderProblem(OrderErrorCode.ORDER_NOT_FOUND)
    record NotFound(UUID orderId) implements DeliverOrderProblem, NotFoundProblem {
        public String title()  { return "Commande introuvable"; }
        public String detail() { return "La commande %s est introuvable".formatted(orderId); }
        public Map<String, ?> properties() { return Map.of("orderId", orderId); }
    }
}
