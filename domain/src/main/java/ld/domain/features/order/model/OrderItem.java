package ld.domain.features.order.model;

import ld.domain.features.product.model.ProductColor;
import ld.domain.valueObjects.Price;
import ld.domain.valueObjects.Quantity;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {
    private final UUID itemId;
    private final UUID productId;
    private final Price unitPrice;
    private final Quantity quantity;
    private Price total;
    private final ProductColor color;


    public OrderItem(UUID itemId, UUID productId, Price unitPrice, Quantity quantity, Price total, ProductColor color) {
        this.itemId = itemId;
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.total = total;
        this.color = color;
    }

    private OrderItem(UUID productId, Price unitPrice, Quantity quantity, ProductColor color) {
        this.itemId = UUID.randomUUID();
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.color = color;
    }

    public static OrderItem create(UUID productId, BigDecimal unitPrice, int quantity, String color) {
        return new OrderItem(productId, new Price(unitPrice), new Quantity(quantity), new ProductColor(color));
    }

    public static OrderItem from(OrderSnapshot.OrderItemSnapshot orderItemSnapshot) {
        return new OrderItem(orderItemSnapshot.itemId(),
                orderItemSnapshot.productId(), orderItemSnapshot.price(),
                new Quantity(orderItemSnapshot.quantity()),
                orderItemSnapshot.total(), orderItemSnapshot.color());
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

    public UUID getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity.value();
    }

    public ProductColor getColor() {
        return color;
    }

    void setTotal(Price newTotal) {
        this.total = newTotal;
    }

    Price getUnitPrice() {
        return unitPrice;
    }
}
