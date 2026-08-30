package ld.domain.features.order.accept;

import ld.domain.features.order.lifecycle.OrderLifecycleRepository;
import ld.domain.features.order.model.Order;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.validation.Result;

public class AcceptOrderUseCaseImpl implements AcceptOrderUseCase {

    private final OrderLifecycleRepository orderLifecycleRepository;
    private final AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher;

    public AcceptOrderUseCaseImpl(OrderLifecycleRepository orderLifecycleRepository,
                                  AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher) {
        this.orderLifecycleRepository = orderLifecycleRepository;
        this.orderEventAggregateEventDispatcher = orderEventAggregateEventDispatcher;
    }

    @Override
    public Result<OrderSnapshot> execute(AcceptOrderCommand acceptOrderCommand) {
        var orderSnapshot = this.orderLifecycleRepository.findById(acceptOrderCommand.orderId())
                .map(Result::success)
                .orElseGet(() -> Result.resourceNotFound(OrderErrorCode.ORDER_NOT_FOUND, "Commande introuvable",
                        String.format("La command %s est introuvable", acceptOrderCommand.orderId())));

        return orderSnapshot.map(Order::from)
                .flatMap(Order::accept)
                .map(order -> {
                    var snapshot = order.toSnapshot();
                    this.orderLifecycleRepository.save(snapshot);
                    order.getDomainEvents().forEach(this.orderEventAggregateEventDispatcher::dispatch);
                    return snapshot;
                });
    }
}
