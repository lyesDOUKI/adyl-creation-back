package ld.application.infra.db.entity;

import jakarta.persistence.*;
import ld.application.infra.db.converter.OrderStatusConverter;
import ld.domain.features.order.model.OrderState;
import ld.domain.features.order.model.OrderStatus;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID id;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customerEntity;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private DeliveryAddressEntity deliveryAddressEntity;

    @Column(length = 255)
    private String customerMessage;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_type", nullable = false, length = 30)
    private OrderState statusType;

    @Convert(converter = OrderStatusConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "status_data", columnDefinition = "jsonb")
    private OrderStatus statusData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "orderEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderDetailEntity> details = new ArrayList<>();

    public OrderEntity() {}

    public OrderEntity(
            UUID orderId,
            CustomerEntity customerEntity,
            DeliveryAddressEntity deliveryAddressEntity,
            String message,
            BigDecimal total,
            OrderStatus orderStatus
    ) {
        this.id = orderId;
        this.customerEntity = customerEntity;
        this.deliveryAddressEntity = deliveryAddressEntity;
        this.customerMessage = message;
        this.total = total;
        this.statusData = orderStatus;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CustomerEntity getCustomer() {
        return customerEntity;
    }

    public void setCustomer(CustomerEntity customerEntity) {
        this.customerEntity = customerEntity;
    }

    public CustomerEntity getCustomerEntity() {
        return customerEntity;
    }

    public void setCustomerEntity(CustomerEntity customerEntity) {
        this.customerEntity = customerEntity;
    }

    public DeliveryAddressEntity getDeliveryAddressEntity() {
        return deliveryAddressEntity;
    }

    public void setDeliveryAddressEntity(DeliveryAddressEntity deliveryAddressEntity) {
        this.deliveryAddressEntity = deliveryAddressEntity;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public void setCustomerMessage(String customerMessage) {
        this.customerMessage = customerMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderDetailEntity> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetailEntity> details) {
        this.details = details;
    }

    public OrderEntity(
            UUID id,
            CustomerEntity customerEntity,
            DeliveryAddressEntity deliveryAddressEntity
    ) {
        this.id = id;
        this.customerEntity = customerEntity;
        this.deliveryAddressEntity = deliveryAddressEntity;
    }

    public void addDetail(OrderDetailEntity detail) {
        details.add(detail);
        detail.assignOrder(this);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public OrderState getStatusType() {
        return statusType;
    }

    public void setStatusType(OrderState statusType) {
        this.statusType = statusType;
    }

    public OrderStatus getStatusData() {
        return statusData;
    }

    public void setStatusData(OrderStatus statusData) {
        this.statusData = statusData;
    }

    @PrePersist
    @PreUpdate
    private void syncStatusType() {
        if (this.statusData != null) {
            OrderState state = this.statusData.type();
            if (state != null) {
                this.statusType = state;
            }
        }
    }
}
