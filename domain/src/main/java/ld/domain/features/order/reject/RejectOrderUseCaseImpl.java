package ld.domain.features.order.reject;

import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.Order;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class RejectOrderUseCaseImpl implements RejectOrderUseCase {

    private final OrderEditor orderEditor;
    private final AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public RejectOrderUseCaseImpl(OrderEditor orderEditor,
                                  AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher,
                                  UnitOfWork unitOfWork,
                                  Clock clock) {
        this.orderEditor = orderEditor;
        this.orderEventAggregateEventDispatcher = orderEventAggregateEventDispatcher;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    @Override
    public Result<OrderSnapshot> execute(RejectOrderCommand rejectOrderCommand) {
        var result = unitOfWork.executeInTransaction(() ->
                rejectOrder(rejectOrderCommand.orderId(), rejectOrderCommand.reason())
        );
        return result
                .map(this::dispatchEvents)
                .map(Order::toSnapshot);
    }

    private Result<OrderSnapshot> findOrder(UUID orderId) {
        return this.orderEditor.findById(orderId)
                .map(Result::success)
                .orElseGet(() -> Result.resourceNotFound(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "Commande introuvable",
                        String.format("La commande %s est introuvable", orderId)
                ));
    }

    private Result<Order> rejectOrder(UUID orderId, String reason) {
        return findOrder(orderId)
                .map(Order::from)
                .flatMap(order -> order.reject(reason, Instant.now(clock)))
                .map(this::save);
    }

    private Order save(Order order) {
        this.orderEditor.save(order.toSnapshot());
        return order;
    }

    private Order dispatchEvents(Order order) {
        order.getDomainEvents().forEach(this.orderEventAggregateEventDispatcher::dispatch);
        order.clearDomainEvents();
        return order;
    }
}
