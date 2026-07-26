package ld.domain.features.product.model;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductSnapshot(UUID productId, String name, BigDecimal price, List<Color> colors, ProductStatus productStatus) {
}
