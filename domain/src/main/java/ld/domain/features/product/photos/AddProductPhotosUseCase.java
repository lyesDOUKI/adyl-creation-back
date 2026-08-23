package ld.domain.features.product.photos;

import ld.domain.features.product.model.ProductSnapshot;
import ld.standard.lib.validation.Result;

public interface AddProductPhotosUseCase {
    Result<ProductSnapshot> execute(AddProductPhotosCommand addProductPhotosCommand);
}
