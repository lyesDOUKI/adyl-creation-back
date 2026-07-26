package ld.domain.features.order.model;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderSnapshot(UUID orderId, String name,
                            String email, String phone,
                            String address, String city,
                            String message, BigDecimal total,
                            OrderStatus orderStatus,
                            List<OrderItemSnapshot> items) {
    public record OrderItemSnapshot(UUID itemId, UUID productId, BigDecimal price,
                                    int quantity, BigDecimal total, Color color){}
}
