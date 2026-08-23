package ld.domain.features.product;

import ld.domain.features.product.model.ProductSnapshot;
import ld.standard.lib.validation.Result;

public interface CreateProductUseCase {
    Result<ProductSnapshot> execute(CreateProductCommand createProductCommand);
}
