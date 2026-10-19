package ld.application.problems.product.photos;

import ld.domain.features.product.photos.validation.AddProductPhotosError;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.ProblemSpecs;

public class AddProductPhotosProblemMapper implements ProblemMapper<AddProductPhotosError> {

    @Override
    public ProblemDetailResponse toProblem(AddProductPhotosError error) {
        return ProblemSpecs.toResponse(toApi(error));
    }

    private AddProductPhotosProblem toApi(AddProductPhotosError error) {
        return switch (error) {
            case AddProductPhotosError.NoPhotos ignored ->
                    new AddProductPhotosProblem.noPhotos();

            case AddProductPhotosError.EmptyPhoto e ->
                    new AddProductPhotosProblem.emptyPhoto(e.fileName());

            case AddProductPhotosError.PhotoTooLarge e ->
                    new AddProductPhotosProblem.photoTooLarge(
                            e.fileName(),
                            e.sizeBytes(),
                            e.maxSizeBytes());

            case AddProductPhotosError.ExtensionNotAllowed e ->
                    new AddProductPhotosProblem.extensionNotAllowed(
                            e.fileName(),
                            e.allowedExtensions());

            case AddProductPhotosError.ProductNotFound e ->
                    new AddProductPhotosProblem.productNotFound(e.givenId());
        };
    }
}