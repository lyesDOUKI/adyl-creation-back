package ld.application.problems.order.create;

import ld.application.problems.order.create.CreateOrderProblem.ColorNotAvailable;
import ld.application.problems.order.create.CreateOrderProblem.UnavailableProducts;
import ld.application.problems.order.create.CreateOrderProblem.UnknownProducts;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

public enum CreateOrderErrorCode implements ApiErrorCode {

    PRODUCTS_NOT_FOUND(UNPROCESSABLE_CONTENT, "Produits introuvables", UnknownProducts.class),
    PRODUCT_NOT_AVAILABLE(CONFLICT, "Produits indisponibles", UnavailableProducts.class),
    PRODUCT_COLOR_NOT_AVAILABLE(UNPROCESSABLE_CONTENT, "Couleur demandée non disponible", ColorNotAvailable.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    CreateOrderErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String title() { return title; }
    @Override public Class<? extends ApiProblem> payload() { return payload; }
}