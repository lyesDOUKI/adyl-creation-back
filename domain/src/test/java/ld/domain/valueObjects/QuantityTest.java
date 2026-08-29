package ld.domain.valueObjects;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {

    @Test
    void should_reject_negative_value() {
        assertThatThrownBy(() -> new Quantity(-5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_zero_value() {
        assertThatThrownBy(() -> new Quantity(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_positive_value() {
        var quantity = new Quantity(3);
        assertThat(quantity.value()).isEqualTo(3);
    }
}