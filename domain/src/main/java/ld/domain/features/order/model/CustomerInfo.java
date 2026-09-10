package ld.domain.features.order.model;

import ld.domain.features.order.CreateOrderCommand;

import java.util.UUID;

public record CustomerInfo(
        UUID identitySubject,
        DeliveryAddress deliveryAddress
) {
    public record DeliveryAddress(
            String address,
            String city
    ) {
    }
    public static CustomerInfo from(UUID identitySubject,CreateOrderCommand.DeliveryInformation customerInfo) {
        return new CustomerInfo(
                identitySubject,
                new DeliveryAddress(
                        customerInfo.address(),
                        customerInfo.city()
                )
        );
    }
}

