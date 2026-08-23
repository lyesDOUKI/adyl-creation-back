package ld.domain.features.product.photos;

import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.standard.lib.validation.Result;

public interface AddProductPhotosUseCase {
    Result<ProductPhotoSnapshot> execute(AddProductPhotosCommand addProductPhotosCommand);
}
