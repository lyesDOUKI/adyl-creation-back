package ld.domain.features.order.cancel;

import ld.domain.features.order.lifecycle.AbstractOrderLifecycleUseCase;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.time.Instant;

public class CancelOrderUseCaseImpl extends AbstractOrderLifecycleUseCase<CancelOrderError> implements CancelOrderUseCase {

    private final Clock clock;
    public CancelOrderUseCaseImpl(OrderEditor orderEditor,
                                  AggregateEventDispatcher<OrderEvent> orderEventDispatcher,
                                  UnitOfWork unitOfWork,
                                  Clock clock) {
        super(orderEditor, orderEventDispatcher, unitOfWork);
        this.clock = clock;
    }

    @Override
    public Result<OrderSnapshot, CancelOrderError> execute(CancelOrderCommand cancelOrderCommand) {
        return executeOperation(cancelOrderCommand.orderId(),
                order -> order.cancel(Instant.now(clock)),
                CancelOrderError.OrderNotFound::new);
    }
}
