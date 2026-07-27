package ld.application.request;

import jakarta.validation.constraints.*;
import ld.domain.features.order.CreateOrderCommand;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String customerName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String customerEmail,

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Numéro de téléphone invalide")
        String customerPhoneNumber,

        String customerAddress,

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

    public CreateOrderCommand toCommand() {
        var customerInfo = new CreateOrderCommand.CustomerInfo(
                customerName,
                customerPhoneNumber,
                customerEmail,
                customerAddress,
                customerCity
        );
        return new CreateOrderCommand(
                customerInfo,
                customerMessage,
                items.stream().map(ItemOrderRequest::to).toList()
        );
    }
}
