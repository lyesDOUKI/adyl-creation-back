package ld.domain.features.order;

import ld.domain.features.product.model.ProductColor;
import ld.standard.lib.validation.DomainError;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public sealed interface OrderCreateError extends DomainError {


    record UnknownProducts(Set<UUID> productIds) implements OrderCreateError {
    }


    record UnavailableProducts(List<UUID> productIds) implements OrderCreateError {
    }


    record ColorNotAvailable(UUID productId, String color, List<ProductColor> availableColors)
            implements OrderCreateError {
    }
}
