package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mode de livraison : PICKUP_POINT (point relais) ou HAND_DELIVERY (remise en main propre)")
public enum DeliveryMethodRequest {
    PICKUP_POINT,
    HAND_DELIVERY
}