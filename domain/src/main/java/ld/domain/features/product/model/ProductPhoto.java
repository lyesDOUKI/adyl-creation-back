package ld.domain.features.product.model;

import java.util.UUID;

public record ProductPhoto(
        UUID id,
        String storageKey,
        int position
) {}
