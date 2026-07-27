package ld.domain.features.product;

import ld.standard.lib.validation.Result;

public interface CreateProductUseCase {
    Result<Void> execute(CreateProductCommand createProductCommand);
}
