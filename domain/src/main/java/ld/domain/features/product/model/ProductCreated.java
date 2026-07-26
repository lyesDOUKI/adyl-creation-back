package ld.domain.features.product.model;

import java.util.UUID;

public record ProductCreated(UUID productId) implements ProductEvent {
}
