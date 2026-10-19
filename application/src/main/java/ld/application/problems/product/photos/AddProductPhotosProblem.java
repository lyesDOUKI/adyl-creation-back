package ld.application.problems.product.photos;

import ld.application.problems.product.ProductProblem;
import ld.application.problems.product.ProductErrorCode;
import ld.spring.web.lib.ApiProblem;
import ld.spring.web.lib.NotFoundProblem;
import ld.spring.web.lib.UnprocessableProblem;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public sealed interface AddProductPhotosProblem extends ApiProblem {

    @PhotoProblem(PhotoErrorCode.PHOTO_LIST_EMPTY)
    record noPhotos() implements AddProductPhotosProblem, UnprocessableProblem {

        public String title() {
            return "Aucune image";
        }

        public String detail() {
            return "La liste des images est vide";
        }

    }

    @PhotoProblem(PhotoErrorCode.PHOTO_SIZE_ZERO)
    record emptyPhoto(String fileName) implements AddProductPhotosProblem, UnprocessableProblem {

        public String title() {
            return "Taille incorrecte";
        }

        public String detail() {
            return "La taille de l'image est à 0 (fichier : %s)".formatted(fileName);
        }

        public Map<String, ?> properties() {
            return Map.of("fileName", fileName);
        }
    }

    @PhotoProblem(PhotoErrorCode.PHOTO_MAX_SIZE_EXCEEDED)
    record photoTooLarge(String fileName, long sizeBytes, long maxSizeBytes)
            implements AddProductPhotosProblem, UnprocessableProblem {

        public String title() {
            return "Taille incorrecte";
        }

        public String detail() {
            return "La taille de l'image dépasse la taille maximum autorisée (%d octets, fichier : %s)"
                    .formatted(maxSizeBytes, fileName);
        }

        public Map<String, ?> properties() {
            return Map.of(
                    "fileName", fileName,
                    "sizeBytes", sizeBytes,
                    "maxSizeBytes", maxSizeBytes
            );
        }
    }

    @PhotoProblem(PhotoErrorCode.PHOTO_EXTENSION_NOT_ALLOWED)
    record extensionNotAllowed(String fileName, Collection<String> allowedExtensions)
            implements AddProductPhotosProblem, UnprocessableProblem {

        public String title() {
            return "Extension incorrecte";
        }

        public String detail() {
            return "L'extension de la photo n'est pas autorisée (fichier : %s, autorisées : %s)"
                    .formatted(fileName, allowedExtensions);
        }

        public Map<String, ?> properties() {
            return Map.of(
                    "fileName", fileName,
                    "allowedExtensions", allowedExtensions
            );
        }
    }

    @ProductProblem(ProductErrorCode.PRODUCT_NOT_FOUND)
    record productNotFound(UUID productId) implements AddProductPhotosProblem, NotFoundProblem {

        public String title() {
            return "Produit introuvable";
        }

        public String detail() {
            return "Le produit %s est introuvable".formatted(productId);
        }

        public Map<String, ?> properties() {
            return Map.of("productId", productId);
        }
    }
}