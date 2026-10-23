package ld.application.problems.product.get;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.ApiProblem;

import java.util.UUID;

import static ld.application.problems.product.get.GetProductErrorCode.PRODUCT_NOT_FOUND;

public sealed interface GetProductProblem extends ApiProblem {

    record ProductNotFound(
            @Schema(description = "Identifiant du produit introuvable") UUID productId
    ) implements GetProductProblem {
        @Override public GetProductErrorCode getCode() { return PRODUCT_NOT_FOUND; }
        @Override public String getDetail() { return "Le produit est introuvable."; }
    }
}