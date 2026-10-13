package ld.domain.features.product.photos.validation;

import ld.domain.features.product.photos.AddProductPhotosCommand;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

import java.util.Set;

public class PhotoRule implements BusinessRule<AddProductPhotosCommand, AddProductPhotosError> {

    public static final long MAX_FILE_SIZE_BYTES = 8 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    @Override
    public Result<Void, AddProductPhotosError> apply(AddProductPhotosCommand command) {
        if (command.photos() == null || command.photos().isEmpty()) {
            return Result.failure(new AddProductPhotosError.NoPhotos());
        }

        return command.photos().stream()
                .map(this::validate)
                .filter(Result::isFailure)
                .findFirst()
                .orElseGet(Result::ok);
    }

    private Result<Void, AddProductPhotosError> validate(AddProductPhotosCommand.PhotoToUpload photo) {
        if (photo.content() == null || photo.content().length == 0) {
            return Result.failure(new AddProductPhotosError.EmptyPhoto(photo.fileName()));
        }
        if (photo.content().length > MAX_FILE_SIZE_BYTES) {
            return Result.failure(new AddProductPhotosError.PhotoTooLarge(
                    photo.fileName(), photo.content().length, MAX_FILE_SIZE_BYTES));
        }
        if (!ALLOWED_EXTENSIONS.contains(extensionOf(photo.fileName()))) {
            return Result.failure(new AddProductPhotosError.ExtensionNotAllowed(
                    photo.fileName(), ALLOWED_EXTENSIONS));
        }
        return Result.ok();
    }

    private String extensionOf(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i >= 0 ? fileName.substring(i).toLowerCase() : "";
    }
}