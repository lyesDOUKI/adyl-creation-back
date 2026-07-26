package ld.domain.features.order.model;

import ld.domain.valueObjects.Price;
import ld.domain.valueObjects.Quantity;

import java.awt.*;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {
    private final UUID itemId;
    private final UUID productId;
    private final Price unitPrice;
    private final Quantity quantity;
    private Price total;
    private final Color color;


    private OrderItem(UUID productId, Price unitPrice, Quantity quantity, Color color) {
        this.itemId = UUID.randomUUID();
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.color = color;
    }

    public static OrderItem create(UUID productId, BigDecimal unitPrice, int quantity, Color color) {
        return new OrderItem(productId, new Price(unitPrice), new Quantity(quantity), color);
    }

    public void calculateTotal() {
        this.total = this.unitPrice.multiply(quantity);
    }

    public Price getTotal() {
        return total;
    }

    public UUID getProductId() {
        return productId;
    }

    public BigDecimal getTotalValue() {
        return total.value();
    }
}
