package ld.application.infra.db.jooq;

import ld.domain.features.product.model.ProductCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineQuery(
        UUID productId,
        String productName,
        ProductCategory productCategory,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String chosenColor,
        BigDecimal discountRate
) {}
