package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.model.OrderState;

@Schema(description = "État d'une commande : PENDING (en attente), ACCEPTED, REJECTED ou DELIVERED")
public enum OrderStateResponse {
    PENDING,
    REJECTED,
    DELIVERED,
    ACCEPTED;

    public static OrderStateResponse from(OrderState orderState) {
        return switch (orderState) {
            case PENDING -> PENDING;
            case REJECTED -> REJECTED;
            case DELIVERED -> DELIVERED;
            case ACCEPTED -> ACCEPTED;
        };
    }
}