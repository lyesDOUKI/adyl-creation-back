package ld.application.request;

import jakarta.validation.constraints.*;
import ld.domain.features.order.CreateOrderCommand;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(

        @NotBlank
        String customerAddress,
        @NotBlank
        String customerCity,

        String customerMessage,

        @NotNull
        @NotEmpty
        List<ItemOrderRequest> items
) {
    public record ItemOrderRequest(
            @NotNull
            UUID productId,
            int quantity,
            String color
    ){
        CreateOrderCommand.CreateOrderItem to() {
            return new CreateOrderCommand.CreateOrderItem(
                    productId,
                    quantity,
                    color
            );
        }
    }

    public CreateOrderCommand toCommand(UUID identitySubject) {
        var deliveryInformation = new CreateOrderCommand.DeliveryInformation(
                customerAddress,
                customerCity
        );
        return new CreateOrderCommand(
                identitySubject,
                deliveryInformation,
                customerMessage,
                items.stream().map(ItemOrderRequest::to).toList()
        );
    }
}
