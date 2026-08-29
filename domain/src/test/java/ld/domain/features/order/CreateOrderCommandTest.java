package ld.domain.features.order;


import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderCommandTest {

    @Test
    void should_reject_non_positive_quantity() {
        assertThatThrownBy(() -> new CreateOrderCommand.CreateOrderItem(UUID.randomUUID(), -5, "red"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_zero_quantity() {
        assertThatThrownBy(() -> new CreateOrderCommand.CreateOrderItem(UUID.randomUUID(), 0, "red"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_positive_quantity() {
        var item = new CreateOrderCommand.CreateOrderItem(UUID.randomUUID(), 3, "red");
        assertThat(item.quantity()).isEqualTo(3);
    }

    @Test
    void should_reject_blank_name() {
        assertThatThrownBy(() -> new CreateOrderCommand.CustomerInfo(" ", "0612345678", "john@doe.com", "1 rue A", "Paris"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_null_name() {
        assertThatThrownBy(() -> new CreateOrderCommand.CustomerInfo(null, "0612345678", "john@doe.com", "1 rue A", "Paris"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_blank_phone_number() {
        assertThatThrownBy(() -> new CreateOrderCommand.CustomerInfo("John Doe", " ", "john@doe.com", "1 rue A", "Paris"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_null_phone_number() {
        assertThatThrownBy(() -> new CreateOrderCommand.CustomerInfo("John Doe", null, "john@doe.com", "1 rue A", "Paris"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_blank_email() {
        assertThatThrownBy(() -> new CreateOrderCommand.CustomerInfo("John Doe", "0612345678", " ", "1 rue A", "Paris"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_reject_null_email() {
        assertThatThrownBy(() -> new CreateOrderCommand.CustomerInfo("John Doe", "0612345678", null, "1 rue A", "Paris"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_accept_valid_customer_info() {
        var customerInfo = new CreateOrderCommand.CustomerInfo("John Doe", "0612345678", "john@doe.com", "1 rue A", "Paris");
        assertThat(customerInfo.name()).isEqualTo("John Doe");
        assertThat(customerInfo.email()).isEqualTo("john@doe.com");
    }
}