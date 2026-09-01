package ld.application.response;

import ld.domain.features.order.model.DeliveryMethod;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.spring.web.lib.ApiResponseBody;

import java.time.Instant;
import java.util.UUID;

public record DeliverOrderResponse(UUID orderId, String observation,
                                   DeliveryMethod deliveryMethod, Instant deliveredAt) implements ApiResponseBody {

    public static DeliverOrderResponse from(OrderSnapshot orderSnapshot) {
        if(!(orderSnapshot.orderStatus() instanceof
                OrderStatus.Delivered(String observation, DeliveryMethod  deliveryMethod, Instant deliveredAt))) {
            throw new IllegalArgumentException(
                    "Order snapshot must have a DELIVERED status"
            );
        }
        return new DeliverOrderResponse(orderSnapshot.orderId(), observation, deliveryMethod, deliveredAt);
    }
}
