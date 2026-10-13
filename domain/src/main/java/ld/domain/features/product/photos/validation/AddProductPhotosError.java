package ld.domain.features.product.photos.validation;

import ld.domain.features.product.validation.ProductErrorCode;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.FailureDetail;

import java.util.Set;
import java.util.UUID;


public sealed interface AddProductPhotosError extends DomainError {

    record NoPhotos() implements AddProductPhotosError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    PhotoErrorCode.PHOTO_LIST_EMPTY,
                    "Aucune image",
                    "La liste des images est vide");
        }
    }

    record EmptyPhoto(String fileName) implements AddProductPhotosError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    PhotoErrorCode.PHOTO_SIZE_ZERO,
                    "Taille incorrecte",
                    "La taille de l'image est à 0 (fichier : " + fileName + ")");
        }
    }

    record PhotoTooLarge(String fileName, long sizeBytes, long maxSizeBytes) implements AddProductPhotosError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    PhotoErrorCode.PHOTO_MAX_SIZE_EXCEEDED,
                    "Taille incorrecte",
                    "La taille de l'image dépasse la taille maximum autorisée (%d octets, fichier : %s)"
                            .formatted(maxSizeBytes, fileName));
        }
    }

    record ExtensionNotAllowed(String fileName, Set<String> allowedExtensions) implements AddProductPhotosError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    PhotoErrorCode.PHOTO_EXTENSION_NOT_ALLOWED,
                    "Extension incorrecte",
                    "L'extension de la photo n'est pas autorisée (fichier : %s, autorisées : %s)"
                            .formatted(fileName, allowedExtensions));
        }
    }

    record ProductNotFound(UUID givenId) implements AddProductPhotosError {

        @Override
        public FailureDetail detail() {
            return FailureDetail.resourceNotFound(
                    ProductErrorCode.PRODUCTS_NOT_FOUND,
                    "Produit introuvable",
                    String.format("Le produit %s est introuvable", givenId)
            );
        }
    }
}