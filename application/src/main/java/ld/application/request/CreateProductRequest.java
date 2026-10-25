package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ld.domain.features.product.CreateProductCommand;
import ld.domain.features.product.model.ProductCategory;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Création d'un produit")
public record CreateProductRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Schema(description = "Nom du produit (unique)", example = "Bonnet en laine")
        String name,

        @NotNull
        @Schema(description = "Catégorie du produit", enumAsRef = true)
        ProductCategoryRequest productCategory,

        @Schema(description = "Couleurs disponibles", example = "[\"rouge\", \"bleu\"]", nullable = true)
        List<String> colors,

        @NotNull
        @Positive
        @Schema(description = "Prix unitaire strictement positif, en euros", example = "24.90")
        BigDecimal price
) {
    public CreateProductCommand to() {
        return new CreateProductCommand(name, to(productCategory), price, colors);
    }

    private static ProductCategory to(ProductCategoryRequest productCategoryRequest) {
        return switch (productCategoryRequest) {
            case ACCESSORIES -> ProductCategory.ACCESSORIES;
            case CLOTHING -> ProductCategory.CLOTHING;
        };
    }
}