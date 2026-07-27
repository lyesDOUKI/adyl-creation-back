package ld.domain.features.product.validation;

import ld.domain.features.product.CreateProductCommand;
import ld.domain.features.product.CreateProductRepository;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

public class ProductNameRule implements BusinessRule<CreateProductCommand> {

    private final CreateProductRepository createProductRepository;

    public ProductNameRule(CreateProductRepository createProductRepository) {
        this.createProductRepository = createProductRepository;
    }

    @Override
    public Result<Void> apply(CreateProductCommand command) {
        if (this.createProductRepository.alreadyExists(command.name())) {
            return Result.businessFailure(
                    ProductErrorCode.PRODUCT_ALREADY_EXISTS,
                    "Produit déjà existant",
                    String.format("Le produit %s existe déjà", command.name()));
        }
        return Result.ok();
    }
}
