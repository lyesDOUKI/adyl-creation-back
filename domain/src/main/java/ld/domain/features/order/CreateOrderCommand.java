package ld.domain.features.order;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record CreateOrderCommand(String name, String phoneNumber, String email, String address, String city,
                                 Optional<String> message, List<CreateOrderItem> createOrderItems) {
    public record CreateOrderItem(UUID productId, BigDecimal unitPrice, int quantity, Color color) {}
}
