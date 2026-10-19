package ld.domain.features.product.validation;

import ld.standard.lib.validation.DomainError;


public sealed interface CreateProductError extends DomainError {
    record ProductNameAlreadyExists(String existingName) implements CreateProductError {
    }
}