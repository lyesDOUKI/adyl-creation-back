package ld.domain.features.product.validation;

import ld.domain.features.product.CreateProductCommand;
import ld.domain.features.product.CreateProductRepository;
import ld.lib.validation.BusinessRule;
import ld.lib.validation.Result;

public class ProductNameRule implements BusinessRule<CreateProductCommand> {

    private final CreateProductRepository createProductRepository;

    public ProductNameRule(CreateProductRepository createProductRepository) {
        this.createProductRepository = createProductRepository;
    }

    @Override
    public Result<Void> apply(CreateProductCommand command) {
        if (this.createProductRepository.alreadyExists(command.name())) {
            return Result.businessFailure("Produit déjà existant", "le produit : " + command.name() + "existe déjà");
        }
        return Result.ok();
    }
}
