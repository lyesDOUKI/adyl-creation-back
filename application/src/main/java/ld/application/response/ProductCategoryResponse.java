package ld.application.response;

import ld.domain.features.product.model.ProductCategory;

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
