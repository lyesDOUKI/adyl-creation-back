package ld.application.shared;

import ld.domain.features.order.CreateOrderCommand;

import java.util.List;
import java.util.UUID;

public class CreateOrderCommandFixture {

    public static CreateOrderCommand aValidCommand(UUID productId) {
        return new CreateOrderCommand(
                new CreateOrderCommand.CustomerInfo("Jean Dupont", "test", "jean@mail.com",
                        "test", "test"),
                "Merci",
                List.of(new CreateOrderCommand.CreateOrderItem(productId, 2, "jaune"))
        );
    }

    public static CreateOrderCommand aValidCommandWithUnknownProduct() {
        return aValidCommand(UUID.randomUUID());
    }
}