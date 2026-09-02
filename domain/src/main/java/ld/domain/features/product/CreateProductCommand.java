package ld.domain.features.product;

import ld.domain.features.product.model.ProductCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CreateProductCommand(
        String name,
        ProductCategory productCategory,
        BigDecimal price,
        List<String> colors
) {
    public CreateProductCommand {
        colors = colors == null
                ? List.of()
                : colors.stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
