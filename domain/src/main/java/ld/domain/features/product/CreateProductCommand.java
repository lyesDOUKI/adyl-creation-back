package ld.domain.features.product;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductCommand(String name, BigDecimal price, List<String> colors) {
}
