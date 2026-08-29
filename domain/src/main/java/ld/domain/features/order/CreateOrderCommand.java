package ld.domain.features.order;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(CustomerInfo customerInfo,
                                 String message,
                                 List<CreateOrderItem> createOrderItems) {

    public record CustomerInfo(String name, String phoneNumber, String email,
                               String address, String city) {
        public CustomerInfo {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new IllegalArgumentException("phoneNumber must not be blank");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("email must not be blank");
            }
        }
    }

    public record CreateOrderItem(UUID productId, int quantity, String color) {
        public CreateOrderItem {
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0, got: " + quantity);
            }
        }
    }
}
