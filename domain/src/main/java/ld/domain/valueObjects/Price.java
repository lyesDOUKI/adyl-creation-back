package ld.domain.valueObjects;

import java.math.BigDecimal;
import java.util.Objects;

public record Price(BigDecimal value) {
    public Price {
        Objects.requireNonNull(value, "Le prix ne peut pas etre null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas etre négatif");
        }
    }

    public static Price zero() {
        return new Price(BigDecimal.ZERO);
    }

    public Price multiply(Quantity quantity) {
        return new Price(
                value.multiply(BigDecimal.valueOf(quantity.value()))
        );
    }

    public Price add(Price other) {
        return new Price(
                value.add(other.value)
        );
    }

    public Price subtract(Price other) {
        return new Price(value.subtract(other.value));
    }

    public Price percentageOf(Percentage percentage) {
        return new Price(value.multiply(percentage.asFraction()));
    }
}
