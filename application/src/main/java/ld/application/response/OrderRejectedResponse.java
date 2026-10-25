package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Commande rejetée")
public record OrderRejectedResponse(
        @Schema(description = "Identifiant de la commande", requiredMode = REQUIRED)
        UUID orderId,

        @Schema(description = "Motif du rejet, s'il a été renseigné", nullable = true)
        String reason,

        @Schema(description = "Montant total de la commande rejetée, en euros", example = "42.50",
                requiredMode = REQUIRED)
        BigDecimal total,

        @Schema(description = "État de la commande (toujours REJECTED)", enumAsRef = true, requiredMode = REQUIRED)
        OrderStateResponse status,

        @Schema(description = "Horodatage du rejet", example = "2026-09-15T07:00:00Z", requiredMode = REQUIRED)
        Instant rejectedAt
) implements ApiResponseBody {

    public static OrderRejectedResponse from(OrderSnapshot orderSnapshot) {
        if (!(orderSnapshot.orderStatus() instanceof OrderStatus.Rejected(String reason, Instant rejectedAt))) {
            throw new IllegalArgumentException("Order snapshot must have a REJECTED status");
        }
        return new OrderRejectedResponse(
                orderSnapshot.orderId(),
                reason,
                orderSnapshot.total(),
                OrderStateResponse.from(orderSnapshot.orderStatus().type()),
                rejectedAt);
    }
}