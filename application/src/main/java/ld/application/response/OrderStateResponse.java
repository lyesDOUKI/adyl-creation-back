package ld.application.response;

import ld.domain.features.order.model.OrderState;

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
