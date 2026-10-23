package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.model.OrderSnapshot;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Commande enregistrée")
public record CreateOrderResponse(
        @Schema(description = "Identifiant de la commande créée", requiredMode = REQUIRED)
        UUID orderId,

        @Schema(description = "Montant total de la commande, en euros", example = "42.50", requiredMode = REQUIRED)
        BigDecimal total
) implements ApiResponseBody {
    public static CreateOrderResponse from(OrderSnapshot orderSnapshot) {
        return new CreateOrderResponse(orderSnapshot.orderId(), orderSnapshot.total());
    }
}