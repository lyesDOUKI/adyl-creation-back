package ld.domain.features.order;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(CustomerInfo customerInfo,
                                 String message,
                                 List<CreateOrderItem> createOrderItems) {
    public record CustomerInfo(String name, String phoneNumber, String email,
                               String address, String city){}
    public record CreateOrderItem(UUID productId, int quantity, String color) {}
}
