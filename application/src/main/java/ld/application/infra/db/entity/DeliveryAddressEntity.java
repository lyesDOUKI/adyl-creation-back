package ld.application.infra.db.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ORDER_DELIVERY_ADDRESSES")
public class DeliveryAddressEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    public DeliveryAddressEntity(String address, String city) {
        this.address = address;
        this.city = city;
    }

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    public DeliveryAddressEntity() {
    }

    public DeliveryAddressEntity(UUID orderId, String address, String city) {
        this.orderId = orderId;
        this.address = address;
        this.city = city;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
