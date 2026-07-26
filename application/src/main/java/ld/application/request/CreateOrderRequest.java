package ld.application.request;

import jakarta.validation.constraints.*;
import ld.domain.features.order.CreateOrderCommand;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record CreateOrderRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String costumerName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String costumerEmail,

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Numéro de téléphone invalide")
        String costumerPhoneNumber,

        String costumerAddress,

        String costumerCity,

        String costumerMessage,

        @NotNull
        @NotEmpty
        List<ItemOrderRequest> items
) {
    public record ItemOrderRequest(
            @NotNull
            UUID productId,
            BigDecimal unitPrice,
            int quantity,
            String color
    ){
        CreateOrderCommand.CreateOrderItem to() {
            return new CreateOrderCommand.CreateOrderItem(
                    productId,
                    unitPrice,
                    quantity,
                    color
            );
        }
    }

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(
                costumerName,
                costumerPhoneNumber,
                costumerEmail,
                costumerAddress,
                costumerCity,
                Optional.ofNullable(costumerMessage),
                items.stream().map(ItemOrderRequest::to).toList()
        );
    }
}
