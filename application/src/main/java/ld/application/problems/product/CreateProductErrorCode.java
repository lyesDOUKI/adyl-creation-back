package ld.application.problems.product;

import ld.application.problems.product.CreateProductProblem.NameAlreadyExists;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

public enum CreateProductErrorCode implements ApiErrorCode {

    PRODUCT_ALREADY_EXISTS(CONFLICT, "Produit déjà existant", NameAlreadyExists.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    CreateProductErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String title() { return title; }
    @Override public Class<? extends ApiProblem> payload() { return payload; }
}