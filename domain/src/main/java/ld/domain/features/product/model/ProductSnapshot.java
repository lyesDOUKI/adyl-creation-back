package ld.domain.features.product.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductSnapshot(UUID productId, String name, BigDecimal price, List<ProductColor> colors, ProductStatus productStatus,
                              List<ProductPhoto> photos) {
}
