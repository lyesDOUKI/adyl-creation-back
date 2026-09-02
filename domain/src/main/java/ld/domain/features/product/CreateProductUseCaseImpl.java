package ld.domain.features.product;

import ld.domain.features.product.model.Product;
import ld.domain.features.product.model.ProductEvent;
import ld.domain.features.product.model.ProductSnapshot;
import ld.domain.features.product.validation.ProductNameRule;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.BusinessGuard;
import ld.standard.lib.validation.Result;

public class CreateProductUseCaseImpl implements CreateProductUseCase {

    private final ProductCreator productCreator;
    private final AggregateEventDispatcher<ProductEvent> aggregateEventDispatcher;
    private final BusinessGuard<CreateProductCommand> createProductGuard;
    private final UnitOfWork unitOfWork;
    public CreateProductUseCaseImpl(ProductCreator productCreator, ProductChecker productChecker,
                                    AggregateEventDispatcher<ProductEvent> aggregateEventDispatcher,
                                    UnitOfWork unitOfWork) {
        this.productCreator = productCreator;
        this.aggregateEventDispatcher = aggregateEventDispatcher;
        this.createProductGuard = initGuard(productChecker);
        this.unitOfWork = unitOfWork;
    }

    private static BusinessGuard<CreateProductCommand> initGuard(ProductChecker productChecker) {
        return BusinessGuard.of(new ProductNameRule(productChecker));
    }

    @Override
    public Result<ProductSnapshot> execute(CreateProductCommand createProductCommand) {
        return this.createProductGuard.validate(createProductCommand)
                .flatMap(_ -> this.unitOfWork.executeInTransaction(() -> {
                    Product product = Product.create(
                            createProductCommand.name(),
                            createProductCommand.price(),
                            createProductCommand.colors(),
                            createProductCommand.productCategory()
                    );
                    this.productCreator.create(product.toSnapshot());
                    return Result.success(product);
                }))
                .map(product -> {
                    product.getDomainEvents().forEach(this.aggregateEventDispatcher::dispatch);
                    return product.toSnapshot();
                });
    }
}
