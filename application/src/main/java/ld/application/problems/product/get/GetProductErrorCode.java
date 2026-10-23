package ld.application.problems.product.get;

import ld.application.problems.product.get.GetProductProblem.ProductNotFound;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public enum GetProductErrorCode implements ApiErrorCode {

    PRODUCT_NOT_FOUND(NOT_FOUND, "Produit introuvable", ProductNotFound.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    GetProductErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String title() { return title; }
    @Override public Class<? extends ApiProblem> payload() { return payload; }
}