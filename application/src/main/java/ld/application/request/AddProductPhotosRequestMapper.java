package ld.application.request;

import ld.domain.features.product.photos.AddProductPhotosCommand;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class AddProductPhotosRequestMapper {

    private AddProductPhotosRequestMapper() {}

    public static AddProductPhotosCommand toCommand(UUID productId, List<MultipartFile> files) {
        List<AddProductPhotosCommand.PhotoToUpload> photos = files.stream()
                .map(AddProductPhotosRequestMapper::toPhotoToUpload)
                .toList();

        return new AddProductPhotosCommand(productId, photos);
    }

    private static AddProductPhotosCommand.PhotoToUpload toPhotoToUpload(MultipartFile file) {
        try {
            return new AddProductPhotosCommand.PhotoToUpload(
                    file.getOriginalFilename(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire le fichier " + file.getOriginalFilename(), e);
        }
    }
}
