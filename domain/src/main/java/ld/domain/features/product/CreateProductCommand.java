package ld.domain.features.product;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public record CreateProductCommand(String name, BigDecimal price, List<Color> colors) {
}
