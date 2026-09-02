package ld.domain.features.order.model;

import ld.domain.valueObjects.Percentage;

public sealed interface AppliedDiscount {
    record None() implements AppliedDiscount {}
    record Claimed(Percentage rate) implements AppliedDiscount {}

    static AppliedDiscount none() {
        return new None();
    }

    static AppliedDiscount claimed(Percentage rate) {
        return new Claimed(rate);
    }
}
