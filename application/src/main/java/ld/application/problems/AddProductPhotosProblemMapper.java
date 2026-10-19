package ld.application.problems;

import ld.domain.features.product.photos.validation.AddProductPhotosError;
import ld.domain.features.product.photos.validation.PhotoErrorCode;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.Problems;

import java.util.Map;

public class AddProductPhotosProblemMapper implements ProblemMapper<AddProductPhotosError> {

    @Override
    public ProblemDetailResponse toProblem(AddProductPhotosError error) {
        return switch (error) {

            case AddProductPhotosError.NoPhotos ignored -> Problems.unprocessable(
                    PhotoErrorCode.PHOTO_LIST_EMPTY,
                    "Aucune image",
                    "La liste des images est vide");

            case AddProductPhotosError.EmptyPhoto e -> Problems.unprocessable(
                    PhotoErrorCode.PHOTO_SIZE_ZERO,
                    "Taille incorrecte",
                    "La taille de l'image est à 0 (fichier : %s)".formatted(e.fileName()),
                    Map.of("fileName", e.fileName()));

            case AddProductPhotosError.PhotoTooLarge e -> Problems.unprocessable(
                    PhotoErrorCode.PHOTO_MAX_SIZE_EXCEEDED,
                    "Taille incorrecte",
                    "La taille de l'image dépasse la taille maximum autorisée (%d octets, fichier : %s)"
                            .formatted(e.maxSizeBytes(), e.fileName()),
                    Map.of("fileName", e.fileName(),
                            "sizeBytes", e.sizeBytes(),
                            "maxSizeBytes", e.maxSizeBytes()));

            case AddProductPhotosError.ExtensionNotAllowed e -> Problems.unprocessable(
                    PhotoErrorCode.PHOTO_EXTENSION_NOT_ALLOWED,
                    "Extension incorrecte",
                    "L'extension de la photo n'est pas autorisée (fichier : %s, autorisées : %s)"
                            .formatted(e.fileName(), e.allowedExtensions()),
                    Map.of("fileName", e.fileName(),
                            "allowedExtensions", e.allowedExtensions()));

            case AddProductPhotosError.ProductNotFound e -> Problems.notFound(
                    ProductErrorCode.PRODUCTS_NOT_FOUND,
                    "Produit introuvable",
                    "Le produit %s est introuvable".formatted(e.givenId()),
                    Map.of("productId", e.givenId()));
        };
    }
}
