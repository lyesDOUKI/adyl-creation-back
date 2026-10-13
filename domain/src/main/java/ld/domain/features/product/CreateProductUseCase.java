package ld.domain.features.product;

import ld.domain.features.product.model.ProductSnapshot;
import ld.domain.features.product.validation.CreateProductError;
import ld.standard.lib.validation.Result;

public interface CreateProductUseCase {
    Result<ProductSnapshot, CreateProductError> execute(CreateProductCommand createProductCommand);
}
