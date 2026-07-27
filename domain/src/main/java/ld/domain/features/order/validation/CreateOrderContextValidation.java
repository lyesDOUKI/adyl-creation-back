package ld.domain.features.order.validation;

import ld.domain.features.order.CreateOrderCommand;
import ld.domain.features.product.model.ProductSnapshot;

import java.util.List;

public record CreateOrderContextValidation(CreateOrderCommand createOrderCommand, List<ProductSnapshot> products) {
}
