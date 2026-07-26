package ld.domain.features.order;

import ld.domain.features.order.model.Order;
import ld.domain.features.order.model.OrderCreated;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderItem;
import ld.domain.features.order.validation.ProductExistsRule;
import ld.lib.AggregateEventDispatcher;
import ld.lib.validation.BusinessGuard;
import ld.lib.validation.Result;

import java.util.List;

public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final CreateOrderRepository createOrderRepository;
    private final AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher;
    private final BusinessGuard<CreateOrderCommand> createOrderGuard;

    public CreateOrderUseCaseImpl(CreateOrderRepository createOrderRepository,
                                  AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher) {
        this.createOrderRepository = createOrderRepository;
        this.aggregateEventDispatcher = aggregateEventDispatcher;
        this.createOrderGuard = businessGuard(createOrderRepository);
    }

    private static BusinessGuard<CreateOrderCommand> businessGuard(CreateOrderRepository createOrderRepository) {
        return BusinessGuard.of(
                new ProductExistsRule(createOrderRepository)
        );
    }
    @Override
    public Result<Void> execute(CreateOrderCommand createOrderCommand) {
        return createOrderGuard.validate(createOrderCommand).flatMap(_ -> {
            var order = Order.create(
                    createOrderCommand.email(),
                    createOrderCommand.phoneNumber(),
                    createOrderCommand.address(),
                    createOrderCommand.city(),
                    createOrderCommand.message().orElse(null)
            );
            List<OrderItem> orderItems = this.initItems(createOrderCommand.createOrderItems());
            order.calculateOrder(orderItems);
            this.aggregateEventDispatcher.dispatch(new OrderCreated(order.getId()));
            this.createOrderRepository.create(order);
            return Result.ok();
        });
    }

    private List<OrderItem> initItems(List<CreateOrderCommand.CreateOrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> OrderItem.create(item.productId(), item.unitPrice(), item.quantity(), item.color()))
                .toList();
    }
}
