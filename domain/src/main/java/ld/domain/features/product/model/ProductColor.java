package ld.domain.features.product.model;

public record ProductColor(String value) {
    public ProductColor {
        value = value.toUpperCase();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
