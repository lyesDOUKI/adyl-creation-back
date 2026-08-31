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
    private ProductEntity productEntity;

    public ProductPhotoEntity(){}

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

    public ProductEntity getProduct() {
        return productEntity;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setProduct(ProductEntity productEntity) {
        this.productEntity = productEntity;
    }
}
