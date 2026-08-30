package ld.domain.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PercentageTest {

    @Test
    @DisplayName("Doit rejeter un pourcentage null")
    void should_reject_null_value() {
        assertThatThrownBy(() -> new Percentage(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Doit rejeter un pourcentage négatif")
    void should_reject_negative_value() {
        assertThatThrownBy(() -> new Percentage(BigDecimal.valueOf(-0.01)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Doit rejeter un pourcentage supérieur à 100")
    void should_reject_value_greater_than_100() {
        assertThatThrownBy(() -> new Percentage(BigDecimal.valueOf(100.01)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Doit accepter des pourcentages valides entre 0 et 100 inclus")
    void should_accept_valid_values() {
        var p0 = new Percentage(BigDecimal.ZERO);
        var p50 = new Percentage(BigDecimal.valueOf(50));
        var p100 = new Percentage(BigDecimal.valueOf(100));

        assertThat(p0.value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(p50.value()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(p100.value()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("La fabrique of() doit créer un pourcentage à partir d'un entier")
    void of_should_create_percentage_from_integer() {
        var percentage = Percentage.of(20);
        assertThat(percentage.value()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    @Test
    @DisplayName("asFraction() doit convertir correctement le pourcentage en fraction décimale")
    void asFraction_should_return_correct_fraction() {
        var percentage = Percentage.of(15);

        var fraction = percentage.asFraction();

        assertThat(fraction).isEqualByComparingTo(BigDecimal.valueOf(0.15));
    }

    @Test
    @DisplayName("asFraction() pour 0% et 100% doit retourner les fractions attendues")
    void asFraction_edge_cases_should_return_correct_fractions() {
        assertThat(Percentage.of(0).asFraction()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(Percentage.of(100).asFraction()).isEqualByComparingTo(BigDecimal.ONE);
    }
}