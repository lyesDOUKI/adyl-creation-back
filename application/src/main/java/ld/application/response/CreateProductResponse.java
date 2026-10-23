package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.product.model.ProductSnapshot;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Produit créé")
public record CreateProductResponse(
        @Schema(description = "Identifiant du produit créé", requiredMode = REQUIRED)
        UUID productId,

        @Schema(description = "Nom du produit", example = "Bonnet en laine", requiredMode = REQUIRED)
        String name,

        @Schema(description = "Prix unitaire, en euros", example = "24.90", requiredMode = REQUIRED)
        BigDecimal price
) implements ApiResponseBody {
    public static CreateProductResponse from(ProductSnapshot productSnapshot) {
        return new CreateProductResponse(productSnapshot.productId(), productSnapshot.name(), productSnapshot.price());
    }
}