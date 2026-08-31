package ld.application.infra.db.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_details")
public class OrderDetailEntity {


    @Id
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private OrderEntity orderEntity;


    @Column(nullable = false)
    private UUID productId;


    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;


    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;


    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;


    private String chosenColor;

    public OrderDetailEntity() {}
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrderEntity getOrder() {
        return orderEntity;
    }

    public void setOrder(OrderEntity orderEntity) {
        this.orderEntity = orderEntity;
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

    public OrderDetailEntity(
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


    protected void assignOrder(OrderEntity orderEntity) {
        this.orderEntity = orderEntity;
    }
}