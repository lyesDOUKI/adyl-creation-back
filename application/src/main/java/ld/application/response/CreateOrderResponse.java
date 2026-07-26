package ld.application.response;

import ld.application.response.utils.ApiResponseBody;
import ld.domain.features.order.model.OrderSnapshot;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderResponse(UUID orderId, BigDecimal total) implements ApiResponseBody {
    public static CreateOrderResponse from(OrderSnapshot orderSnapshot) {
        return new CreateOrderResponse(orderSnapshot.orderId(), orderSnapshot.total());
    }
}
