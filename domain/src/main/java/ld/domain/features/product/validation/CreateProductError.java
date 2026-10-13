package ld.domain.features.product.validation;

import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.FailureDetail;


public sealed interface CreateProductError extends DomainError {
    record ProductNameAlreadyExists(String existingName) implements CreateProductError {

        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    ProductErrorCode.PRODUCT_ALREADY_EXISTS,
                    "Produit déjà existant",
                    String.format("Le produit %s existe déjà", existingName));
        }
    }
}