package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Produit du catalogue")
public record GetProductResponse(
        @Schema(description = "Identifiant du produit", requiredMode = REQUIRED)
        UUID productId,

        @Schema(description = "Catégorie du produit", enumAsRef = true, requiredMode = REQUIRED)
        ProductCategoryResponse productCategory,

        @Schema(description = "Nom du produit", example = "Bonnet en laine", requiredMode = REQUIRED)
        String name,

        @Schema(description = "Prix unitaire, en euros", example = "24.90", requiredMode = REQUIRED)
        BigDecimal price,

        @Schema(description = "Couleurs disponibles", example = "[\"rouge\", \"bleu\"]", requiredMode = REQUIRED)
        List<String> colors,

        @Schema(description = "URL des photos du produit, dans l'ordre d'affichage", requiredMode = REQUIRED)
        List<String> photosUri,

        @Schema(description = "Nombre de commandes contenant ce produit", minimum = "0", requiredMode = REQUIRED)
        int numberOfOrders
) implements ApiResponseBody {
}