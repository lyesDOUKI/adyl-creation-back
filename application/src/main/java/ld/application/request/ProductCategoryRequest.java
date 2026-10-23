package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Catégorie de produit : ACCESSORIES (accessoires) ou CLOTHING (vêtements)")
public enum ProductCategoryRequest {
    ACCESSORIES,
    CLOTHING
}