package ld.domain.features.product.photos.validation;

import ld.standard.lib.validation.DomainError;

import java.util.Set;
import java.util.UUID;


public sealed interface AddProductPhotosError extends DomainError {

    record NoPhotos() implements AddProductPhotosError {
    }

    record EmptyPhoto(String fileName) implements AddProductPhotosError {
    }

    record PhotoTooLarge(String fileName, long sizeBytes, long maxSizeBytes) implements AddProductPhotosError {
    }

    record ExtensionNotAllowed(String fileName, Set<String> allowedExtensions) implements AddProductPhotosError {
    }

    record ProductNotFound(UUID givenId) implements AddProductPhotosError {
    }
}