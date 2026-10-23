// AddProductPhotosResponse.java
package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.product.model.ProductPhoto;
import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import ld.spring.web.lib.ApiResponseBody;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Photos d'un produit après ajout")
public record AddProductPhotosResponse(
        @Schema(description = "Identifiant du produit", requiredMode = REQUIRED)
        UUID productId,

        @Schema(description = "Photos du produit, triées par position", requiredMode = REQUIRED)
        List<ProductPhotoResponse> photos
) implements ApiResponseBody {

    @Schema(description = "Photo d'un produit")
    public record ProductPhotoResponse(
            @Schema(description = "Identifiant de la photo", requiredMode = REQUIRED)
            UUID id,

            @Schema(description = "URL de téléchargement de la photo", requiredMode = REQUIRED)
            String url,

            @Schema(description = "Position de la photo dans la galerie du produit", minimum = "0",
                    requiredMode = REQUIRED)
            int position
    ) {}

    public static AddProductPhotosResponse from(ProductPhotoSnapshot snapshot, ProductPhotoUrlResolver urlResolver) {
        List<ProductPhotoResponse> photos = snapshot.photos().stream()
                .sorted(Comparator.comparingInt(ProductPhoto::position))
                .map(photo -> new ProductPhotoResponse(
                        photo.id(),
                        urlResolver.resolve(snapshot.productSnapshot().productId(), photo.storageKey()),
                        photo.position()))
                .toList();

        return new AddProductPhotosResponse(snapshot.productSnapshot().productId(), photos);
    }
}