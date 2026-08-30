package ld.domain.valueObjects;

import java.math.BigDecimal;
import java.util.Objects;

public record Percentage(BigDecimal value) {
    public static final Percentage ZERO = Percentage.of(0);
    public Percentage {
        Objects.requireNonNull(value, "Le pourcentage ne peut pas être null");
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Le pourcentage ne peut pas être négatif ou supérieur à 100");
        }
    }

    public static Percentage of(int value) {
        return new Percentage(BigDecimal.valueOf(value));
    }

    public BigDecimal asFraction() {
        return value.multiply(BigDecimal.valueOf(0.01));
    }
}
