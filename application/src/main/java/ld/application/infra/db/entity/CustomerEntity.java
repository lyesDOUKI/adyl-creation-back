package ld.application.infra.db.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "CUSTOMERS")
public class CustomerEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @Column(name = "customer_name", nullable = false, length = 250)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_address")
    private String customerAddress;

    @Column(name = "customer_city", length = 50)
    private String customerCity;

    public CustomerEntity() {
    }

    public CustomerEntity(String customerName, String customerEmail, String customerPhone, String customerAddress, String customerCity) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.customerCity = customerCity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerCity() {
        return customerCity;
    }

    public void setCustomerCity(String customerCity) {
        this.customerCity = customerCity;
    }
}
