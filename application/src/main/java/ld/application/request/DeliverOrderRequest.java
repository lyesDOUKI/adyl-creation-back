package ld.application.request;

import jakarta.validation.constraints.NotNull;
import ld.domain.features.order.deliver.DeliverOrderCommand;
import ld.domain.features.order.model.DeliveryMethod;

import java.util.UUID;

public record DeliverOrderRequest(
        @NotNull DeliveryMethodRequest deliveryMethod,
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