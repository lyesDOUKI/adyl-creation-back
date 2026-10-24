package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ld.domain.features.order.CreateOrderCommand;

import java.util.List;
import java.util.UUID;

@Schema(description = "Demande de commande")
public record CreateOrderRequest(

        @NotNull
        @NotBlank
        @Schema(description = "test", requiredMode = Schema.RequiredMode.REQUIRED)
        String test,

        @NotBlank
        @Schema(description = "Adresse de livraison", example = "12 rue des Lilas")
        String customerAddress,

        @NotBlank
        @Schema(description = "Ville de livraison", example = "Avignon")
        String customerCity,

        @Schema(description = "Message libre du client pour la commande", example = "Merci de livrer le matin",
                nullable = true)
        String customerMessage,

        @NotEmpty
        @Schema(description = "Produits commandés (au moins un)")
        List<@Valid ItemOrderRequest> items
) {
    @Schema(description = "Ligne d'une demande de commande")
    public record ItemOrderRequest(
            @NotNull
            @Schema(description = "Identifiant du produit commandé")
            UUID productId,

            @Positive(message = "La quantité doit être strictement positive")
            @Schema(description = "Quantité commandée", minimum = "1", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
            int quantity,

            @Schema(description = "Couleur choisie, parmi celles proposées par le produit", example = "rouge",
                    nullable = true)
            String color
    ) {
        CreateOrderCommand.CreateOrderItem to() {
            return new CreateOrderCommand.CreateOrderItem(productId, quantity, color);
        }
    }

    public CreateOrderCommand toCommand(UUID identitySubject) {
        var deliveryInformation = new CreateOrderCommand.DeliveryInformation(customerAddress, customerCity);
        return new CreateOrderCommand(
                identitySubject,
                deliveryInformation,
                customerMessage,
                items.stream().map(ItemOrderRequest::to).toList());
    }
}