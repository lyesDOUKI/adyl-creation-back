package ld.application.shared;

import ld.domain.features.product.CreateProductCommand;

import java.math.BigDecimal;
import java.util.List;

public final class CreateProductCommandFixture {

    private CreateProductCommandFixture() {}

    public static CreateProductCommand aValidCommand() {
        return new CreateProductCommand(
                "T-shirt basique",
                BigDecimal.valueOf(19.99),
                List.of("Rouge", "Bleu")
        );
    }

    public static CreateProductCommand aCommandWithName(String name) {
        return new CreateProductCommand(
                name,
                BigDecimal.valueOf(29.99),
                List.of("Vert")
        );
    }
}
