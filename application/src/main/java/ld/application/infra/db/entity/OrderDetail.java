package ld.application.infra.db.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_details")
public class OrderDetail {


    @Id
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;


    @Column(nullable = false)
    private UUID productId;


    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;


    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;


    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;


    private String chosenColor;

    public OrderDetail() {}
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getChosenColor() {
        return chosenColor;
    }

    public void setChosenColor(String chosenColor) {
        this.chosenColor = chosenColor;
    }

    public OrderDetail(
            UUID id,
            UUID productId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            String chosenColor
    ) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.chosenColor = chosenColor;
    }


    protected void assignOrder(Order order) {
        this.order = order;
    }
}