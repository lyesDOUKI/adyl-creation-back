package ld.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {

    @Test
    @DisplayName("Doit rejeter un prix null")
    void should_reject_null_value() {
        assertThatThrownBy(() -> new Price(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Doit rejeter un prix négatif")
    void should_reject_negative_value() {
        assertThatThrownBy(() -> new Price(BigDecimal.valueOf(-10)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Doit accepter une valeur à zéro")
    void should_accept_zero_value() {
        var price = new Price(BigDecimal.ZERO);
        assertThat(price.value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Doit accepter une valeur positive et appliquer l'arrondi monétaire")
    void should_accept_positive_value() {
        var price = new Price(BigDecimal.valueOf(19.99));
        assertThat(price.value()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
    }

    @Test
    @DisplayName("La fabrique zero() doit retourner un prix à zéro")
    void zero_should_return_price_with_zero_value() {
        var price = Price.zero();
        assertThat(price.value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("La multiplication par une quantité doit retourner le bon montant")
    void multiply_should_return_price_multiplied_by_quantity() {
        var price = new Price(BigDecimal.valueOf(10));
        var quantity = new Quantity(3);

        var result = price.multiply(quantity);

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    @DisplayName("La multiplication par un doit retourner un prix équivalent")
    void multiply_by_one_should_return_equivalent_price() {
        var price = new Price(BigDecimal.valueOf(10));
        var quantity = new Quantity(1);

        var result = price.multiply(quantity);

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    @DisplayName("L'addition de deux prix doit retourner leur somme")
    void add_should_return_sum_of_both_prices() {
        var price1 = new Price(BigDecimal.valueOf(10));
        var price2 = new Price(BigDecimal.valueOf(5.5));

        var result = price1.add(price2);

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(15.5));
    }

    @Test
    @DisplayName("L'addition avec zéro doit retourner un prix équivalent")
    void add_zero_should_return_equivalent_price() {
        var price = new Price(BigDecimal.valueOf(10));

        var result = price.add(Price.zero());

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    @DisplayName("La soustraction de deux prix doit retourner leur différence")
    void subtract_should_return_difference_between_prices() {
        var price1 = new Price(BigDecimal.valueOf(15.5));
        var price2 = new Price(BigDecimal.valueOf(5.5));

        var result = price1.subtract(price2);

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    @DisplayName("Le calcul d'un pourcentage du prix doit retourner le montant correct")
    void percentageOf_should_return_calculated_percentage() {
        var price = new Price(BigDecimal.valueOf(100));
        var percentage = Percentage.of(10);

        var result = price.percentageOf(percentage);

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }
}