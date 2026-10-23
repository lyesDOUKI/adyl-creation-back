package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.model.DeliveryMethod;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.spring.web.lib.ApiResponseBody;

import java.time.Instant;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Commande livrée")
public record OrderDeliveredResponse(
        @Schema(description = "Identifiant de la commande", requiredMode = REQUIRED)
        UUID orderId,

        @Schema(description = "Observation saisie lors de la livraison, s'il y en a une", nullable = true)
        String observation,

        @Schema(description = "Mode de livraison", enumAsRef = true, requiredMode = REQUIRED)
        DeliveryMethodResponse deliveryMethod,

        @Schema(description = "État de la commande (toujours DELIVERED)", enumAsRef = true, requiredMode = REQUIRED)
        OrderStateResponse orderState,

        @Schema(description = "Horodatage de la livraison", example = "2026-09-15T07:00:00Z", requiredMode = REQUIRED)
        Instant deliveredAt
) implements ApiResponseBody {

    public static OrderDeliveredResponse from(OrderSnapshot orderSnapshot) {
        if (!(orderSnapshot.orderStatus() instanceof
                OrderStatus.Delivered(String observation, DeliveryMethod deliveryMethod, Instant deliveredAt))) {
            throw new IllegalArgumentException("Order snapshot must have a DELIVERED status");
        }
        return new OrderDeliveredResponse(
                orderSnapshot.orderId(),
                observation,
                toResponse(deliveryMethod),
                OrderStateResponse.from(orderSnapshot.orderStatus().type()),
                deliveredAt);
    }

    private static DeliveryMethodResponse toResponse(DeliveryMethod deliveryMethod) {
        return switch (deliveryMethod) {
            case PICKUP_POINT -> DeliveryMethodResponse.PICKUP_POINT;
            case HAND_DELIVERY -> DeliveryMethodResponse.HAND_DELIVERY;
        };
    }
}