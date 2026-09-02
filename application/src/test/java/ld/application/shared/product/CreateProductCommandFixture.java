package ld.application.shared.product;

import ld.domain.features.product.CreateProductCommand;
import ld.domain.features.product.model.ProductCategory;

import java.math.BigDecimal;
import java.util.List;

public final class CreateProductCommandFixture {

    private CreateProductCommandFixture() {}

    public static CreateProductCommand aValidCommand() {
        return new CreateProductCommand(
                "T-shirt basique",
                ProductCategory.ACCESSORIES,
                BigDecimal.valueOf(19.99),
                List.of("Rouge", "Bleu")
        );
    }

    public static CreateProductCommand aCommandWithName(String name) {
        return new CreateProductCommand(
                name,
                ProductCategory.ACCESSORIES,
                BigDecimal.valueOf(29.99),
                List.of("Vert")
        );
    }
}
