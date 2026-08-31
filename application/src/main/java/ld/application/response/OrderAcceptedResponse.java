package ld.application.response;

import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderState;
import ld.domain.features.order.model.OrderStatus;
import ld.domain.valueObjects.Percentage;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderAcceptedResponse(
        UUID orderId,
        OrderState orderState,
        Instant acceptedAt,
        BigDecimal discountRate
) implements ApiResponseBody {

    public static OrderAcceptedResponse from(OrderSnapshot orderSnapshot) {

        if (!(orderSnapshot.orderStatus() instanceof OrderStatus.Accepted(
                Instant at, Percentage discountApplied
        ))) {
            throw new IllegalArgumentException(
                    "Order snapshot must have an ACCEPTED status"
            );
        }

        return new OrderAcceptedResponse(
                orderSnapshot.orderId(),
                OrderState.ACCEPTED,
                at,
                discountApplied.value()
        );
    }
}
