package ld.application.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineResponse(
        UUID productId,
        String productName,
        ProductCategoryResponse productCategory,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String chosenColor,
        BigDecimal subtotalBeforeDiscount,
        BigDecimal discountRate,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {}