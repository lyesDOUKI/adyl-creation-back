package ld.domain.features.order.model;

import ld.domain.features.product.model.ProductColor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderSnapshot(UUID orderId, Customer customer,
                            String message, BigDecimal total,
                            OrderStatus orderStatus,
                            List<OrderItemSnapshot> items) {
    public record OrderItemSnapshot(UUID itemId, UUID productId, BigDecimal price,
                                    int quantity, BigDecimal total, ProductColor color){}
}
