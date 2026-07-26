package ld.domain.features.product;

import ld.lib.validation.Result;

public interface CreateProductUseCase {
    Result<Void> execute(CreateProductCommand createProductCommand);
}
