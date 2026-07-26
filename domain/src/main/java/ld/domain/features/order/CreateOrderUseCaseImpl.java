package ld.domain.features.order;

import ld.domain.features.order.model.*;
import ld.domain.features.product.GetProductRepository;
import ld.domain.features.product.model.ProductSnapshot;
import ld.lib.AggregateEventDispatcher;
import ld.lib.validation.Result;

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

    public CreateOrderUseCaseImpl(CreateOrderRepository createOrderRepository,
                                  GetProductRepository getProductRepository,
                                  AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher) {
        this.createOrderRepository = createOrderRepository;
        this.getProductRepository = getProductRepository;
        this.aggregateEventDispatcher = aggregateEventDispatcher;
    }

    @Override
    public Result<OrderSnapshot> execute(CreateOrderCommand createOrderCommand) {
        return this.initItems(createOrderCommand.createOrderItems())
                .map(orderItems -> {
                    var order = Order.create(
                            createOrderCommand.name(),
                            createOrderCommand.email(),
                            createOrderCommand.phoneNumber(),
                            createOrderCommand.address(),
                            createOrderCommand.city(),
                            createOrderCommand.message()
                    );
                    order.calculateOrder(orderItems);
                    this.createOrderRepository.create(order.toSnapshot());
                    this.aggregateEventDispatcher.dispatch(new OrderCreated(order.getId()));
                    return order.toSnapshot();
                });
    }

    private Result<List<OrderItem>> initItems(List<CreateOrderCommand.CreateOrderItem> orderItems) {
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
                    "Produits introuvables",
                    "Des produits de la commande sont introuvables (identifiants : " + missingIds + ")"
            );
        }

        List<OrderItem> items = orderItems.stream()
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

        return Result.success(items);
    }
}
