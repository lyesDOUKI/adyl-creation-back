package ld.domain.features.order;

import ld.domain.valueObjects.Price;
import ld.domain.valueObjects.Quantity;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record CreateOrderCommand(String phoneNumber, String email, String address, String city,
                                 Optional<String> message, List<CreateOrderItem> createOrderItems) {
    public record CreateOrderItem(UUID productId, BigDecimal unitPrice, int quantity, Color color) {}
}
