package ld.domain.features.order.model;

import ld.domain.features.product.model.ProductColor;
import ld.domain.valueObjects.Percentage;
import ld.domain.valueObjects.Price;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderSnapshot(UUID orderId, CustomerInfo customerInfo,
                            String message, BigDecimal total,
                            OrderStatus orderStatus,
                            List<OrderItemSnapshot> items) {
    public record OrderItemSnapshot(UUID itemId,
                                    UUID productId,
                                    Price price,
                                    int quantity,
                                    Price total,
                                    Percentage discountRate,
                                    ProductColor color){}
}
