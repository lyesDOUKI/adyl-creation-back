package ld.domain.features.product.validation;

import ld.domain.features.product.CreateProductCommand;
import ld.domain.features.product.ProductChecker;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

public class ProductNameRule implements BusinessRule<CreateProductCommand> {

    private final ProductChecker productChecker;

    public ProductNameRule(ProductChecker productChecker) {
        this.productChecker = productChecker;
    }

    @Override
    public Result<Void> apply(CreateProductCommand command) {
        if (this.productChecker.alreadyExists(command.name())) {
            return Result.businessFailure(
                    ProductErrorCode.PRODUCT_ALREADY_EXISTS,
                    "Produit déjà existant",
                    String.format("Le produit %s existe déjà", command.name()));
        }
        return Result.ok();
    }
}
