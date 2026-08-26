package ld.domain.features.product.model;

public record ProductColor(String value) {
    public ProductColor {
        value = value == null ? null : value.toUpperCase();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
