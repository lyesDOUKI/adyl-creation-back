package ld.domain.features.order;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(UUID identitySubject,
                                 DeliveryInformation deliveryInformation,
                                 String message,
                                 List<CreateOrderItem> createOrderItems) {

    public record DeliveryInformation(String address, String city) {
    }

    public record CreateOrderItem(UUID productId, int quantity, String color) {
        public CreateOrderItem {
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0, got: " + quantity);
            }
        }
    }
}
