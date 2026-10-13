package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.Order;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractOrderLifecycleUseCase {
    protected final OrderEditor orderEditor;
    private final AggregateEventDispatcher<OrderEvent> orderEventDispatcher;
    private final UnitOfWork unitOfWork;

    protected AbstractOrderLifecycleUseCase(
            OrderEditor orderEditor,
            AggregateEventDispatcher<OrderEvent> orderEventDispatcher,
            UnitOfWork unitOfWork) {
        this.orderEditor = orderEditor;
        this.orderEventDispatcher = orderEventDispatcher;
        this.unitOfWork = unitOfWork;
    }

    protected Result<OrderSnapshot, OrderLifecycleError> executeOperation(
            UUID orderId,
            Function<Order, Result<Order, OrderLifecycleError>> operation) {

        Result<Order, OrderLifecycleError> result = unitOfWork.executeInTransaction(() ->
                findOrder(orderId)
                        .map(Order::from)
                        .andThen(operation)
                        .map(this::save)
        );
        return result.map(this::dispatchEvents).map(Order::toSnapshot);
    }

    private Result<OrderSnapshot, OrderLifecycleError> findOrder(UUID orderId) {
        return orderEditor.findById(orderId)
                .map(Result::<OrderSnapshot, OrderLifecycleError>success)
                .orElseGet(() -> Result.failure(new OrderLifecycleError.OrderNotFound(orderId)));
    }

    private Order save(Order order) {
        orderEditor.save(order.toSnapshot());
        return order;
    }

    private Order dispatchEvents(Order order) {
        order.getDomainEvents().forEach(orderEventDispatcher::dispatch);
        order.clearDomainEvents();
        return order;
    }
}