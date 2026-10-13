package ld.domain.features.order;

import ld.domain.features.order.model.*;
import ld.domain.features.order.validation.CreateOrderContextValidation;
import ld.domain.features.order.validation.ProductStatusRule;
import ld.domain.features.order.validation.ProductsColorsRule;
import ld.domain.features.product.ProductFinder;
import ld.domain.features.product.model.ProductSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.BusinessGuard;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderCreator orderCreator;
    private final ProductFinder productFinder;
    private final AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher;
    private final BusinessGuard<CreateOrderContextValidation, OrderCreateError> createOrderGuard;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public CreateOrderUseCaseImpl(OrderCreator orderCreator,
                                  ProductFinder productFinder,
                                  AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher,
                                  UnitOfWork unitOfWork, Clock clock) {
        this.orderCreator = orderCreator;
        this.productFinder = productFinder;
        this.aggregateEventDispatcher = aggregateEventDispatcher;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
        this.createOrderGuard = BusinessGuard.of(
                new ProductStatusRule(),
                new ProductsColorsRule()
        );
    }

    @Override
    public Result<OrderSnapshot, OrderCreateError> execute(CreateOrderCommand command) {
        return this.getGivenProducts(command.createOrderItems())
                .andThen(givenProducts -> this.assertGivenProducts(command, givenProducts))
                .map(productsById -> this.initItems(command.createOrderItems(), productsById))
                .andThen(orderItems -> this.createOrder(command, orderItems))
                .map(this::dispatchEventsAndSnapshot);
    }

    private Result<Map<UUID, ProductSnapshot>, OrderCreateError> getGivenProducts(
            List<CreateOrderCommand.CreateOrderItem> orderItems) {

        List<UUID> requestedIds = orderItems.stream()
                .map(CreateOrderCommand.CreateOrderItem::productId)
                .toList();

        Map<UUID, ProductSnapshot> productsById = this.productFinder
                .findAllBy(requestedIds)
                .stream()
                .collect(Collectors.toMap(ProductSnapshot::productId, Function.identity()));

        Set<UUID> missingIds = requestedIds.stream()
                .filter(id -> !productsById.containsKey(id))
                .collect(Collectors.toSet());

        return missingIds.isEmpty()
                ? Result.success(productsById)
                : Result.failure(new OrderCreateError.UnknownProducts(missingIds));
    }

    private Result<Map<UUID, ProductSnapshot>, OrderCreateError> assertGivenProducts(
            CreateOrderCommand command,
            Map<UUID, ProductSnapshot> givenProducts) {

        return this.createOrderGuard
                .validate(new CreateOrderContextValidation(command, List.copyOf(givenProducts.values())))
                .map(_ -> givenProducts);
    }

    private List<OrderItem> initItems(List<CreateOrderCommand.CreateOrderItem> orderItems,
                                      Map<UUID, ProductSnapshot> productsById) {
        return orderItems.stream()
                .map(item -> {
                    ProductSnapshot product = productsById.get(item.productId());
                    return OrderItem.create(product.productId(), product.price(), item.quantity(), item.color());
                })
                .toList();
    }

    private Result<Order, OrderCreateError> createOrder(CreateOrderCommand command, List<OrderItem> orderItems) {
        return this.unitOfWork.executeInTransaction(() -> {
            var order = Order.create(
                    CustomerInfo.from(command.identitySubject(), command.deliveryInformation()),
                    command.message(), clock
            );
            order.calculateOrder(orderItems);
            this.orderCreator.create(order.toSnapshot());
            return Result.success(order);
        });
    }

    private OrderSnapshot dispatchEventsAndSnapshot(Order order) {
        order.getDomainEvents().forEach(this.aggregateEventDispatcher::dispatch);
        return order.toSnapshot();
    }
}