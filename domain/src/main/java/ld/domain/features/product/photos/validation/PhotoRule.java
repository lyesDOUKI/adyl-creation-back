package ld.domain.features.product.photos.validation;

import ld.domain.features.product.photos.AddProductPhotosCommand;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

import java.util.Set;

public class PhotoRule implements BusinessRule<AddProductPhotosCommand> {

    public static final long MAX_FILE_SIZE_BYTES = 8 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    @Override
    public Result<Void> apply(AddProductPhotosCommand command) {
        if (command.photos() == null || command.photos().isEmpty()) {
            return Result.businessFailure(PhotoErrorCode.PHOTO_LIST_EMPTY, "Aucune images", "La liste des images est vide");
        }

        for (var photo : command.photos()) {
            if (photo.content() == null || photo.content().length == 0) {
                return Result.businessFailure(PhotoErrorCode.PHOTO_SIZE_ZERO, "Taille incorrect", "La taille de l'image est à 0");
            }
            if (photo.content().length > MAX_FILE_SIZE_BYTES) {
                return Result.businessFailure(PhotoErrorCode.PHOTO_MAX_SIZE_EXCEEDED, "Taille incorrect",
                        String.format("La taille de l'image a dépassé la taille maximum autorisé (%d)", MAX_FILE_SIZE_BYTES));
            }
            if (!ALLOWED_EXTENSIONS.contains(getExtension(photo.fileName()))) {
                return Result.businessFailure(PhotoErrorCode.PHOTO_EXTENSION_NOT_ALLOWED, "Extension incorrect",
                        String.format("L'extension de la photo donnée n'est pas autorisé (%s)", ALLOWED_EXTENSIONS));
            }
        }
        return Result.ok();
    }

    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i >= 0 ? fileName.substring(i).toLowerCase() : "";
    }
}
