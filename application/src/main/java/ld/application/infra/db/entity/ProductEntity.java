package ld.application.infra.db.entity;

import jakarta.persistence.*;
import ld.domain.features.product.model.ProductStatus;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @ElementCollection
    @CollectionTable(
            name = "product_colors",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "color")
    private Set<String> colors = new HashSet<>();

    @OneToMany(mappedBy = "productEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductPhotoEntity> photos = new ArrayList<>();

    public ProductEntity() {}
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public Set<String> getColors() {
        return colors;
    }

    public void setColors(Set<String> colors) {
        this.colors = colors;
    }

    public List<ProductPhotoEntity> getPhotos() {
        return photos;
    }

    public void addPhoto(ProductPhotoEntity photo) {
        this.photos.add(photo);
    }
    public void setPhotos(List<ProductPhotoEntity> photos) {
        this.photos = photos;
    }
}