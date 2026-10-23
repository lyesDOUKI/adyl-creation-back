package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ld.domain.features.order.reject.RejectOrderCommand;

import java.util.UUID;

@Schema(description = "Rejet d'une commande")
public record RejectOrderRequest(
        @Size(max = 500, message = "Le motif ne doit pas dépasser 500 caractères")
        @Schema(description = "Motif du rejet, facultatif", example = "Produit épuisé", maxLength = 500,
                nullable = true)
        String reason
) {
    public RejectOrderCommand to(UUID orderId) {
        return new RejectOrderCommand(orderId, reason);
    }
}