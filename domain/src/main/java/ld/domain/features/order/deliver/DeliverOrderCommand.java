package ld.domain.features.order.deliver;

import ld.domain.features.order.model.DeliveryMethod;

import java.util.UUID;

public record DeliverOrderCommand(
        UUID orderId,
        DeliveryMethod deliveryMethod,
        String observation
) {}
