package ld.bootstrap.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Commande rejetée")
public record OrderCancelledResponse(
        @Schema(description = "Identifiant de la commande", requiredMode = REQUIRED)
        UUID orderId,

        @Schema(description = "Montant total de la commande annulée, en euros", example = "42.50",
                requiredMode = REQUIRED)
        BigDecimal total,

        @Schema(description = "État de la commande (toujours CANCELLED)", enumAsRef = true, requiredMode = REQUIRED)
        OrderStateResponse status,

        @Schema(description = "Horodatage du rejet", example = "2026-09-15T07:00:00Z", requiredMode = REQUIRED)
        Instant cancelledAt
) implements ApiResponseBody {

    public static OrderCancelledResponse from(OrderSnapshot orderSnapshot) {
        if (!(orderSnapshot.orderStatus() instanceof OrderStatus.Cancelled(Instant cancelledAt))) {
            throw new IllegalArgumentException("Order snapshot must have a CANCELLED status");
        }
        return new OrderCancelledResponse(
                orderSnapshot.orderId(),
                orderSnapshot.total(),
                OrderStateResponse.from(orderSnapshot.orderStatus().type()),
                cancelledAt);
    }
}
