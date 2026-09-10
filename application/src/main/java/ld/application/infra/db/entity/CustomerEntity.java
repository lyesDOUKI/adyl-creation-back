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

    @Column(name = "identity_subject", nullable = false, unique = true, updatable = false)
    private UUID identitySubject;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    public CustomerEntity() {
    }

    public CustomerEntity(UUID identitySubject, String email, String phone) {
        this.identitySubject = identitySubject;
        this.email = email;
        this.phone = phone;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdentitySubject() {
        return identitySubject;
    }

    public void setIdentitySubject(UUID identitySubject) {
        this.identitySubject = identitySubject;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
