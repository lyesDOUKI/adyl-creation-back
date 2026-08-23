package ld.domain.features.product.photos;

import java.util.List;
import java.util.UUID;

public record AddProductPhotosCommand(
        UUID productId,
        List<PhotoToUpload> photos
) {
    public record PhotoToUpload(String fileName, byte[] content) {}
}
