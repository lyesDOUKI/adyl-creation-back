package ld.domain.features.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CreateProductCommand(
        String name,
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
