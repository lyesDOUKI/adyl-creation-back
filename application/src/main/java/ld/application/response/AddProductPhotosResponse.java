package ld.application.response;

import ld.domain.features.product.model.ProductPhoto;
import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import ld.spring.web.lib.ApiResponseBody;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record AddProductPhotosResponse(
        UUID productId,
        List<ProductPhotoResponse> photos
) implements ApiResponseBody {

    public record ProductPhotoResponse(
            UUID id,
            String url,
            int position
    ) {}

    public static AddProductPhotosResponse from(ProductPhotoSnapshot snapshot, ProductPhotoUrlResolver urlResolver) {
        List<ProductPhotoResponse> photos = snapshot.photos().stream()
                .sorted(Comparator.comparingInt(ProductPhoto::position))
                .map(photo -> new ProductPhotoResponse(
                        photo.id(),
                        urlResolver.resolve(photo.storageKey()),
                        photo.position()
                ))
                .toList();

        return new AddProductPhotosResponse(snapshot.productSnapshot().productId(), photos);
    }
}