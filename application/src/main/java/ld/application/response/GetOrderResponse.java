// GetOrderResponse.java
package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Commande d'un client")
public record GetOrderResponse(
        @Schema(description = "Identifiant de la commande", requiredMode = REQUIRED)
        UUID orderId,

        @Schema(description = "Identifiant du client", requiredMode = REQUIRED)
        UUID customerId,

        @Schema(description = "Référence lisible de la commande", requiredMode = REQUIRED)
        String orderReference,

        @Schema(description = "Message laissé par le client, s'il y en a un", nullable = true)
        String customerMessage,

        @Schema(description = "Date de création (UTC)", example = "2026-09-15T07:00:00Z",
                requiredMode = REQUIRED)
        Instant createdAt,

        @Schema(description = "Date de dernière mise à jour (UTC)", example = "2026-09-15T07:30:00Z",
                requiredMode = REQUIRED)
        Instant updatedAt,

        @Schema(description = "Montant total, en euros", example = "42.50", requiredMode = REQUIRED)
        BigDecimal total,

        @Schema(description = "Statut de la commande (valeurs de OrderState : PENDING, ACCEPTED, REJECTED, DELIVERED)",
                requiredMode = REQUIRED)
        OrderStateResponse status,

        @Schema(description = "Lignes de la commande", requiredMode = REQUIRED)
        List<OrderLineResponse> lines,

        @Schema(description = "Nombre de lignes", minimum = "0", requiredMode = REQUIRED)
        int lineCount,

        @Schema(description = "Quantité totale, tous produits confondus", requiredMode = REQUIRED)
        int totalQuantity
) implements ApiResponseBody {}