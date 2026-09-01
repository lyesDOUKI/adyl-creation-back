package ld.application.shared;

import ld.domain.features.product.photos.AddProductPhotosCommand;

import java.util.List;
import java.util.UUID;

public final class AddProductPhotosCommandFixture {

    private AddProductPhotosCommandFixture() {}

    public static AddProductPhotosCommand aValidCommand(UUID productId) {
        return new AddProductPhotosCommand(productId, List.of(
                new AddProductPhotosCommand.PhotoToUpload("photo1.jpg", new byte[]{1, 2, 3}),
                new AddProductPhotosCommand.PhotoToUpload("photo2.png", new byte[]{4, 5, 6})
        ));
    }

    public static AddProductPhotosCommand aValidCommandWithUnknownProduct() {
        return aValidCommand(UUID.randomUUID());
    }

    public static AddProductPhotosCommand aCommandWithDisallowedExtension(UUID productId) {
        return new AddProductPhotosCommand(productId, List.of(
                new AddProductPhotosCommand.PhotoToUpload("document.pdf", new byte[]{1, 2, 3})
        ));
    }
}
