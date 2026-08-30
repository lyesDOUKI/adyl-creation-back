package ld.domain.features.order.model;

import ld.domain.features.order.CreateOrderCommand;

public record CustomerInfo(String name, String email, String phoneNumber, String address, String city) {
    public static CustomerInfo from(CreateOrderCommand.CustomerInfo customerInfo) {
        return new CustomerInfo(customerInfo.name(), customerInfo.email(), customerInfo.phoneNumber(), customerInfo.address(), customerInfo.city());
    }
}
