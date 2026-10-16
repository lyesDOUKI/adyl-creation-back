package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.Order;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.Result;

import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractOrderLifecycleUseCase<E extends DomainError> {

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

    protected final Result<OrderSnapshot, E> executeOperation(
            UUID orderId,
            Function<Order, Result<Order, E>> operation,
            Function<UUID, E> orderNotFoundErrorFactory) {

        Result<Order, E> result = unitOfWork.executeInTransaction(() ->
                findOrder(orderId, orderNotFoundErrorFactory)
                        .map(Order::from)
                        .andThen(operation)
                        .map(this::save)
        );

        return result
                .map(this::dispatchEvents)
                .map(Order::toSnapshot);
    }

    private Result<OrderSnapshot, E> findOrder(
            UUID orderId,
            Function<UUID, E> orderNotFoundErrorFactory) {

        return orderEditor.findById(orderId)
                .map(Result::<OrderSnapshot, E>success)
                .orElseGet(() -> Result.failure(orderNotFoundErrorFactory.apply(orderId)));
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