package ld.application.infra.db.entity;

import jakarta.persistence.*;
import ld.domain.features.order.model.OrderSnapshot;

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

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal discountRate;

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
            BigDecimal discountRate,
            String chosenColor
    ) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.discountRate = discountRate;
        this.chosenColor = chosenColor;
    }

    public void updateFromSnapshot(OrderSnapshot.OrderItemSnapshot item) {
        this.productId = item.productId();
        this.quantity = BigDecimal.valueOf(item.quantity());
        this.unitPrice = item.price().value();
        this.totalAmount = item.total().value();
        this.discountRate = item.discountRate().asFraction();
        this.chosenColor = item.color().value();
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    protected void assignOrder(OrderEntity orderEntity) {
        this.orderEntity = orderEntity;
    }
}