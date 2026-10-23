package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ld.domain.features.order.deliver.DeliverOrderCommand;
import ld.domain.features.order.model.DeliveryMethod;

import java.util.UUID;

@Schema(description = "Livraison d'une commande")
public record DeliverOrderRequest(
        @NotNull
        @Schema(description = "Mode de livraison", enumAsRef = true)
        DeliveryMethodRequest deliveryMethod,

        @Schema(description = "Observation libre sur la livraison", example = "Remis au voisin", nullable = true)
        String observation
) {
    public DeliverOrderCommand toCommand(UUID orderId) {
        return new DeliverOrderCommand(orderId, toDomain(deliveryMethod), observation);
    }

    private static DeliveryMethod toDomain(DeliveryMethodRequest requestMethod) {
        return switch (requestMethod) {
            case PICKUP_POINT -> DeliveryMethod.PICKUP_POINT;
            case HAND_DELIVERY -> DeliveryMethod.HAND_DELIVERY;
        };
    }
}