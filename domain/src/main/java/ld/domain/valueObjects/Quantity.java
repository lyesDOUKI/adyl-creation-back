package ld.domain.valueObjects;

public record Quantity(int value) {
    public Quantity {
        if (value <= 0) {
            throw new IllegalArgumentException("Une quantité ne peut pas etre négatif");
        }
    }
}
