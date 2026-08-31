package ld.application.infra.db.entity;

import jakarta.persistence.*;
import ld.domain.features.order.model.DiscountType;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "discount_claims")
public class DiscountClaimEntity {

    @EmbeddedId
    private DiscountClaimId id;

    @CreationTimestamp
    @Column(name = "claimed_at", nullable = false, updatable = false)
    private Instant claimedAt;

    protected DiscountClaimEntity() {
        // Constructeur requis par JPA
    }

    public DiscountClaimEntity(DiscountType type, String email) {
        this.id = new DiscountClaimId(type, email);
    }

    public DiscountClaimId getId() {
        return id;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }

    // --- Clé Composite Embeddable ---

    @Embeddable
    public static class DiscountClaimId implements Serializable {

        @Enumerated(EnumType.STRING)
        @Column(name = "type", nullable = false, length = 50)
        private DiscountType type;

        @Column(name = "email", nullable = false, length = 255)
        private String email;

        public DiscountClaimId() {
        }

        public DiscountClaimId(DiscountType type, String email) {
            this.type = type;
            this.email = email;
        }

        public DiscountType getType() {
            return type;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DiscountClaimId that = (DiscountClaimId) o;
            return type == that.type && Objects.equals(email, that.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, email);
        }
    }
}