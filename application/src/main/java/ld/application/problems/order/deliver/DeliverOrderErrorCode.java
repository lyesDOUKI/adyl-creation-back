package ld.application.problems.order.deliver;

import ld.application.problems.order.deliver.DeliverOrderProblem.NotFound;
import ld.application.problems.order.deliver.DeliverOrderProblem.Pending;
import ld.application.problems.order.deliver.DeliverOrderProblem.Rejected;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public enum DeliverOrderErrorCode implements ApiErrorCode {

    ORDER_NOT_FOUND(NOT_FOUND, "Commande introuvable", NotFound.class),
    ORDER_ALREADY_REJECTED(CONFLICT, "Commande rejetée", Rejected.class),
    ORDER_STILL_PENDING(CONFLICT, "Commande en attente", Pending.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    DeliverOrderErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String title() { return title; }
    @Override public Class<? extends ApiProblem> payload() { return payload; }
}