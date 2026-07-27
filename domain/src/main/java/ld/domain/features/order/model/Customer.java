package ld.domain.features.order.model;

import ld.domain.features.order.CreateOrderCommand;

public record Customer(String name, String email, String phoneNumber, String address, String city) {
    public static Customer from(CreateOrderCommand.CustomerInfo customerInfo) {
        return new Customer(customerInfo.name(), customerInfo.email(), customerInfo.phoneNumber(), customerInfo.address(), customerInfo.city());
    }
}
