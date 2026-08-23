package ld.application.infra.db.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_photos")
public class ProductPhotoEntity {

    @Id
    private UUID id;

    @Column(name = "storage_key", nullable = false, length = 300)
    private String storageKey;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public ProductPhotoEntity(){}
    public ProductPhotoEntity(UUID id, String storageKey, int position, Product product) {
        this.id = id;
        this.storageKey = storageKey;
        this.position = position;
        this.product = product;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public int getPosition() {
        return position;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
