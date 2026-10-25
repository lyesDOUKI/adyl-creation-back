package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Ligne d'une commande")
public record OrderLineResponse(
        @Schema(description = "Identifiant du produit", requiredMode = REQUIRED)
        UUID productId,

        @Schema(description = "Nom du produit au moment de la commande", requiredMode = REQUIRED)
        String productName,

        @Schema(description = "Catégorie du produit", enumAsRef = true, requiredMode = REQUIRED)
        ProductCategoryResponse productCategory,

        @Schema(description = "Quantité commandée", requiredMode = REQUIRED)
        int quantity,

        @Schema(description = "Prix unitaire, en euros", example = "24.90", requiredMode = REQUIRED)
        BigDecimal unitPrice,

        @Schema(description = "Couleur choisie, si le produit en propose", nullable = true)
        String chosenColor,

        @Schema(description = "Sous-total avant remise, en euros", requiredMode = REQUIRED)
        BigDecimal subtotalBeforeDiscount,

        @Schema(description = "Taux de remise appliqué à la ligne", requiredMode = REQUIRED)
        BigDecimal discountRate,

        @Schema(description = "Montant de la remise, en euros", requiredMode = REQUIRED)
        BigDecimal discountAmount,

        @Schema(description = "Total de la ligne après remise, en euros", requiredMode = REQUIRED)
        BigDecimal totalAmount
) {}