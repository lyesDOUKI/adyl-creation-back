package ld.domain.features.order;

import ld.domain.features.order.model.*;
import ld.domain.features.order.validation.CreateOrderContextValidation;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.features.order.validation.ProductStatusRule;
import ld.domain.features.order.validation.ProductsColorsRule;
import ld.domain.features.product.GetProductRepository;
import ld.domain.features.product.model.ProductSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.BusinessGuard;
import ld.standard.lib.validation.Result;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final CreateOrderRepository createOrderRepository;
    private final GetProductRepository getProductRepository;
    private final AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher;
    private final BusinessGuard<CreateOrderContextValidation> createOrderGuard;
    private final UnitOfWork unitOfWork;

    public CreateOrderUseCaseImpl(CreateOrderRepository createOrderRepository,
                                  GetProductRepository getProductRepository,
                                  AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher,
                                  UnitOfWork unitOfWork) {
        this.createOrderRepository = createOrderRepository;
        this.getProductRepository = getProductRepository;
        this.aggregateEventDispatcher = aggregateEventDispatcher;
        this.unitOfWork = unitOfWork;
        this.createOrderGuard = BusinessGuard.of(
                new ProductStatusRule(),
                new ProductsColorsRule()
        );
    }

    @Override
    public Result<OrderSnapshot> execute(CreateOrderCommand createOrderCommand) {
        return this.getGivenProducts(createOrderCommand.createOrderItems())
                .flatMap(givenProducts ->
                        this.assertGivenProducts(createOrderCommand, givenProducts))
                .map(productsById ->
                        this.initItems(createOrderCommand.createOrderItems(), productsById))
                .flatMap(orderItems -> this.unitOfWork.executeInTransaction(() -> {
                    var order = Order.create(
                            CustomerInfo.from(createOrderCommand.customerInfo()),
                            createOrderCommand.message()
                    );
                    order.calculateOrder(orderItems);
                    this.createOrderRepository.create(order.toSnapshot());
                    return Result.success(order);
                }))
                .map(order -> {
                    order.getDomainEvents().forEach(this.aggregateEventDispatcher::dispatch);
                    return order.toSnapshot();
                });
    }

    private Result<Map<UUID, ProductSnapshot>> getGivenProducts(List<CreateOrderCommand.CreateOrderItem> orderItems) {
        List<UUID> requestedIds = orderItems.stream()
                .map(CreateOrderCommand.CreateOrderItem::productId)
                .toList();

        Map<UUID, ProductSnapshot> productsById = this.getProductRepository
                .getAllBy(requestedIds)
                .stream()
                .collect(Collectors.toMap(ProductSnapshot::productId, Function.identity()));

        Set<UUID> missingIds = requestedIds.stream()
                .filter(id -> !productsById.containsKey(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            return Result.resourceNotFound(
                    OrderErrorCode.PRODUCTS_NOT_FOUND,
                    "Produits introuvables",
                    "Des produits de la commande sont introuvables (identifiants : " + missingIds + ")"
            );
        }

        return Result.success(productsById);
    }

    private Result<Map<UUID, ProductSnapshot>> assertGivenProducts(
            CreateOrderCommand createOrderCommand,
            Map<UUID, ProductSnapshot> givenProducts
    ) {
        return this.createOrderGuard.validate(new CreateOrderContextValidation(
                createOrderCommand, givenProducts.values().stream().toList()
        )).map(_ -> givenProducts);
    }

    private List<OrderItem> initItems(List<CreateOrderCommand.CreateOrderItem> orderItems,
                                      Map<UUID, ProductSnapshot> productsById) {
        return orderItems.stream()
                .map(item -> {
                    ProductSnapshot product = productsById.get(item.productId());
                    return OrderItem.create(
                            product.productId(),
                            product.price(),
                            item.quantity(),
                            item.color()
                    );
                })
                .toList();
    }
}