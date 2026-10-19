package ld.application.problems.order.accept;

import ld.application.problems.order.OrderProblem;
import ld.spring.web.lib.ApiProblem;
import ld.spring.web.lib.ConflictProblem;
import ld.spring.web.lib.NotFoundProblem;
import ld.application.problems.order.OrderErrorCode;

import java.util.Map;
import java.util.UUID;

public sealed interface AcceptOrderProblem extends ApiProblem {

    @OrderProblem(OrderErrorCode.ORDER_ALREADY_REJECTED)
    record Rejected() implements AcceptOrderProblem, ConflictProblem {
        public String title()  { return "Commande rejetée"; }
        public String detail() { return "La commande a été rejetée, elle ne peut plus être acceptée."; }
    }

    @OrderProblem(OrderErrorCode.ORDER_ALREADY_DELIVERED)
    record Delivered() implements AcceptOrderProblem, ConflictProblem {
        public String title()  { return "Commande livrée"; }
        public String detail() { return "La commande a déjà été livrée, elle ne peut plus être acceptée."; }
    }

    @OrderProblem(OrderErrorCode.ORDER_NOT_FOUND)
    record NotFound(UUID orderId) implements AcceptOrderProblem, NotFoundProblem {
        public String title()  { return "Commande introuvable"; }
        public String detail() { return "La commande %s est introuvable".formatted(orderId); }
        public Map<String, ?> properties() { return Map.of("orderId", orderId); }
    }
}