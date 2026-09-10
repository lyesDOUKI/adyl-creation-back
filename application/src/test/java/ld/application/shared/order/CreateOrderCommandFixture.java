package ld.application.shared.order;

import ld.domain.features.order.CreateOrderCommand;

import java.util.List;
import java.util.UUID;

public class CreateOrderCommandFixture {

    public static CreateOrderCommand aValidCommand(
            UUID productId,
            UUID customerIdentitySubject
    ) {
        return new CreateOrderCommand(
                customerIdentitySubject,
                new CreateOrderCommand.DeliveryInformation("test", "test"),
                "Merci",
                List.of(new CreateOrderCommand.CreateOrderItem(productId, 2, "jaune"))
        );
    }

    public static CreateOrderCommand aValidCommand(UUID productId) {
        return aValidCommand(productId, UUID.randomUUID());
    }

    public static CreateOrderCommand aValidCommandWithUnknownProduct() {
        return aValidCommand(UUID.randomUUID());
    }
}