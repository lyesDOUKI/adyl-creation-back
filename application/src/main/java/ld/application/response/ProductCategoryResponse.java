package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.product.model.ProductCategory;

@Schema(description = "Catégorie de produit : ACCESSORIES (accessoires) ou CLOTHING (vêtements)")
public enum ProductCategoryResponse {
    ACCESSORIES,
    CLOTHING;

    public static ProductCategoryResponse from(ProductCategory productCategory) {
        return switch (productCategory) {
            case ACCESSORIES -> ACCESSORIES;
            case CLOTHING -> CLOTHING;
        };
    }
}