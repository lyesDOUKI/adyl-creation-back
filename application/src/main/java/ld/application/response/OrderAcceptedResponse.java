package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.domain.valueObjects.Percentage;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Commande acceptée")
public record OrderAcceptedResponse(
        @Schema(description = "Identifiant de la commande", requiredMode = REQUIRED)
        UUID orderId,

        @Schema(
                description = "Taux de remise de première commande appliqué, entre 0 et 100 (ex: 15 %, 0 = aucune remise)",
                requiredMode = REQUIRED,
                minimum = "0",
                maximum = "100"
        )
        BigDecimal discountRate,

        @Schema(description = "État de la commande (toujours ACCEPTED)", enumAsRef = true, requiredMode = REQUIRED)
        OrderStateResponse status,

        @Schema(description = "Horodatage de l'acceptation", example = "2026-09-15T07:00:00Z", requiredMode = REQUIRED)
        Instant acceptedAt
) implements ApiResponseBody {

    public static OrderAcceptedResponse from(OrderSnapshot orderSnapshot) {
        if (!(orderSnapshot.orderStatus() instanceof OrderStatus.Accepted(
                Instant acceptedAt, Percentage discountApplied
        ))) {
            throw new IllegalArgumentException("Order snapshot must have an ACCEPTED status");
        }

        return new OrderAcceptedResponse(
                orderSnapshot.orderId(),
                discountApplied.value(),
                OrderStateResponse.from(orderSnapshot.orderStatus().type()),
                acceptedAt);
    }
}