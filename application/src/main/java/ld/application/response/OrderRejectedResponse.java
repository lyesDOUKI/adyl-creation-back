package ld.application.response;

import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderRejectedResponse(
        UUID orderId,
        String reason,
        BigDecimal total,
        OrderStateResponse orderState,
        Instant rejectedAt
) implements ApiResponseBody {

    public static OrderRejectedResponse from(OrderSnapshot orderSnapshot) {
        if (!(orderSnapshot.orderStatus() instanceof OrderStatus.Rejected(String reason, Instant rejectedAt))) {
            throw new IllegalArgumentException(
                    "Order snapshot must have a REJECTED status"
            );
        }
        return new OrderRejectedResponse(
                orderSnapshot.orderId(),
                reason,
                orderSnapshot.total(),
                OrderStateResponse.from(orderSnapshot.orderStatus().type()),
                rejectedAt
        );
    }
}
