package ld.application.problems.product;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.product.validation.CreateProductError;
import ld.spring.web.lib.ApiProblem;

import static ld.application.problems.product.CreateProductErrorCode.PRODUCT_ALREADY_EXISTS;

public sealed interface CreateProductProblem extends ApiProblem {

    record NameAlreadyExists(
            @Schema(description = "Nom de produit déjà utilisé") String name
    ) implements CreateProductProblem {
        @Override public CreateProductErrorCode getCode() { return PRODUCT_ALREADY_EXISTS; }
        @Override public String getDetail() { return "Un produit portant ce nom existe déjà."; }
    }

    static CreateProductProblem from(CreateProductError error) {
        return switch (error) {
            case CreateProductError.ProductNameAlreadyExists e -> new NameAlreadyExists(e.existingName());
        };
    }
}