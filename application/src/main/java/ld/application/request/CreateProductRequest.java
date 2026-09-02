package ld.application.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ld.domain.features.product.CreateProductCommand;
import ld.domain.features.product.model.ProductCategory;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String name,

        @NotNull
        ProductCategoryRequest productCategoryRequest,
        List<String> colors,

        @NotNull
        @Positive
        BigDecimal price
)
{
    public CreateProductCommand to() {
        return new CreateProductCommand(name, to(productCategoryRequest), price, colors);
    }
    private static ProductCategory to(ProductCategoryRequest productCategoryRequest) {
        return switch (productCategoryRequest) {
            case ACCESSORIES -> ProductCategory.ACCESSORIES;
            case CLOTHING -> ProductCategory.CLOTHING;
        };
    }
}
