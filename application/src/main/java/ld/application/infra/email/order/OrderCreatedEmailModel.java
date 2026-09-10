package ld.application.infra.email.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEmailModel(
        UUID orderId,
        String customerEmail,
        String customerMessage,
        String createdAt,
        BigDecimal total,
        List<ItemModel> items
) {
    public record ItemModel(
            String productName,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            String chosenColor,
            BigDecimal discountRate
    ) {}
}