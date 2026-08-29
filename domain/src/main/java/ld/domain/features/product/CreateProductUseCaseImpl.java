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

    private final CreateProductRepository createProductRepository;
    private final AggregateEventDispatcher<ProductEvent> aggregateEventDispatcher;
    private final BusinessGuard<CreateProductCommand> createProductGuard;
    private final UnitOfWork unitOfWork;
    public CreateProductUseCaseImpl(CreateProductRepository createProductRepository,
                                    AggregateEventDispatcher<ProductEvent> aggregateEventDispatcher,
                                    UnitOfWork unitOfWork) {
        this.createProductRepository = createProductRepository;
        this.aggregateEventDispatcher = aggregateEventDispatcher;
        this.createProductGuard = initGuard(createProductRepository);
        this.unitOfWork = unitOfWork;
    }

    private static BusinessGuard<CreateProductCommand> initGuard(CreateProductRepository createProductRepository) {
        return BusinessGuard.of(new ProductNameRule(createProductRepository));
    }

    @Override
    public Result<ProductSnapshot> execute(CreateProductCommand createProductCommand) {
        return this.createProductGuard.validate(createProductCommand)
                .flatMap(_ -> this.unitOfWork.executeInTransaction(() -> {
                    Product product = Product.create(
                            createProductCommand.name(),
                            createProductCommand.price(),
                            createProductCommand.colors()
                    );
                    this.createProductRepository.create(product.toSnapshot());
                    return Result.success(product);
                }))
                .map(product -> {
                    product.getDomainEvents().forEach(this.aggregateEventDispatcher::dispatch);
                    return product.toSnapshot();
                });
    }
}
