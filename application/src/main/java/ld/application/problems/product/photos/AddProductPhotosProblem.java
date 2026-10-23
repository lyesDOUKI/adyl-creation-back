package ld.application.problems.product.photos;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.product.photos.validation.AddProductPhotosError;
import ld.spring.web.lib.ApiProblem;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static ld.application.problems.product.photos.AddProductPhotosErrorCode.*;

public sealed interface AddProductPhotosProblem extends ApiProblem {

    record NoPhotos() implements AddProductPhotosProblem {
        @Override public AddProductPhotosErrorCode getCode() { return PHOTO_LIST_EMPTY; }
        @Override public String getDetail() { return "La liste des images est vide."; }
    }

    record EmptyPhoto(
            @Schema(description = "Nom du fichier concerné") String fileName
    ) implements AddProductPhotosProblem {
        @Override public AddProductPhotosErrorCode getCode() { return PHOTO_SIZE_ZERO; }
        @Override public String getDetail() {
            return "La taille de l'image est à 0 (fichier : %s).".formatted(fileName);
        }
    }

    record PhotoTooLarge(
            @Schema(description = "Nom du fichier concerné") String fileName,
            @Schema(description = "Taille du fichier, en octets") long sizeBytes,
            @Schema(description = "Taille maximum autorisée, en octets") long maxSizeBytes
    ) implements AddProductPhotosProblem {
        @Override public AddProductPhotosErrorCode getCode() { return PHOTO_MAX_SIZE_EXCEEDED; }
        @Override public String getDetail() {
            return "La taille de l'image dépasse le maximum autorisé (%d octets, fichier : %s)."
                    .formatted(maxSizeBytes, fileName);
        }
    }

    record ExtensionNotAllowed(
            @Schema(description = "Nom du fichier concerné") String fileName,
            @Schema(description = "Extensions autorisées") SortedSet<String> allowedExtensions
    ) implements AddProductPhotosProblem {
        @Override public AddProductPhotosErrorCode getCode() { return PHOTO_EXTENSION_NOT_ALLOWED; }
        @Override public String getDetail() {
            return "L'extension de la photo n'est pas autorisée (fichier : %s, autorisées : %s)."
                    .formatted(fileName, allowedExtensions);
        }
    }

    record ProductNotFound(
            @Schema(description = "Identifiant du produit introuvable") UUID productId
    ) implements AddProductPhotosProblem {
        @Override public AddProductPhotosErrorCode getCode() { return PRODUCT_NOT_FOUND; }
        @Override public String getDetail() { return "Le produit est introuvable."; }
    }

    static AddProductPhotosProblem from(AddProductPhotosError error) {
        return switch (error) {
            case AddProductPhotosError.NoPhotos _ -> new NoPhotos();
            case AddProductPhotosError.EmptyPhoto e -> new EmptyPhoto(e.fileName());
            case AddProductPhotosError.PhotoTooLarge e ->
                    new PhotoTooLarge(e.fileName(), e.sizeBytes(), e.maxSizeBytes());
            case AddProductPhotosError.ExtensionNotAllowed e ->
                    new ExtensionNotAllowed(e.fileName(), new TreeSet<>(e.allowedExtensions()));
            case AddProductPhotosError.ProductNotFound e -> new ProductNotFound(e.givenId());
        };
    }
}