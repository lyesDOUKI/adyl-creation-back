package ld.bootstrap.problems.order.cancel;

import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

import static ld.bootstrap.problems.order.cancel.CancelOrderProblem.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public enum CancelOrderErrorCode implements ApiErrorCode {
    DELIVERED(CONFLICT, "Commande livrée", Delivered.class),
    ACCEPTED(CONFLICT, "Commande acceptée", Accepted.class),
    REJECTED(CONFLICT, "Commande rejetée", Rejected.class),
    ORDER_NOT_FOUND(NOT_FOUND, "Commande introuvable", NotFound.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    CancelOrderErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public Class<? extends ApiProblem> payload() {
        return payload;
    }
}
