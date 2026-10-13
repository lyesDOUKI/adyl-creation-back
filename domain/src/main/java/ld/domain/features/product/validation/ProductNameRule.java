package ld.domain.features.product.validation;

import ld.domain.features.product.CreateProductCommand;
import ld.domain.features.product.ProductChecker;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

public class ProductNameRule implements BusinessRule<CreateProductCommand, CreateProductError> {

    private final ProductChecker productChecker;

    public ProductNameRule(ProductChecker productChecker) {
        this.productChecker = productChecker;
    }

    @Override
    public Result<Void, CreateProductError> apply(CreateProductCommand command) {
        if (this.productChecker.alreadyExists(command.name())) {
            return Result.failure(new CreateProductError.ProductNameAlreadyExists(command.name()));
        }
        return Result.ok();
    }
}
