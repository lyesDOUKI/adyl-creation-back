package ld.domain.valueObjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {

    @Test
    void should_reject_null_value() {
        assertThatThrownBy(() -> new Price(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_reject_negative_value() {
        assertThatThrownBy(() -> new Price(BigDecimal.valueOf(-10)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_zero_value() {
        var price = new Price(BigDecimal.ZERO);
        assertThat(price.value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_accept_positive_value() {
        var price = new Price(BigDecimal.valueOf(19.99));
        assertThat(price.value()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
    }

    @Test
    void zero_should_return_price_with_zero_value() {
        var price = Price.zero();
        assertThat(price.value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void multiply_should_return_price_multiplied_by_quantity() {
        var price = new Price(BigDecimal.valueOf(10));
        var quantity = new Quantity(3);

        var result = price.multiply(quantity);

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    void multiply_by_one_should_return_equivalent_price() {
        var price = new Price(BigDecimal.valueOf(10));
        var quantity = new Quantity(1);

        var result = price.multiply(quantity);

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }


    @Test
    void add_should_return_sum_of_both_prices() {
        var price1 = new Price(BigDecimal.valueOf(10));
        var price2 = new Price(BigDecimal.valueOf(5.5));

        var result = price1.add(price2);

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(15.5));
    }

    @Test
    void add_zero_should_return_equivalent_price() {
        var price = new Price(BigDecimal.valueOf(10));

        var result = price.add(Price.zero());

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }
}