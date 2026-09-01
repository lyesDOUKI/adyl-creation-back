package ld.application.response;

import ld.domain.features.order.model.DeliveryMethod;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.spring.web.lib.ApiResponseBody;

import java.time.Instant;
import java.util.UUID;

public record OrderDeliveredResponse(UUID orderId,
                                     String observation,
                                     DeliveryMethodResponse deliveryMethod,
                                     OrderStateResponse orderState,
                                     Instant deliveredAt) implements ApiResponseBody {

    public static OrderDeliveredResponse from(OrderSnapshot orderSnapshot) {
        if(!(orderSnapshot.orderStatus() instanceof
                OrderStatus.Delivered(String observation, DeliveryMethod  deliveryMethod, Instant deliveredAt))) {
            throw new IllegalArgumentException(
                    "Order snapshot must have a DELIVERED status"
            );
        }
        return new OrderDeliveredResponse(orderSnapshot.orderId(),
                observation,toResponse(deliveryMethod),
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
