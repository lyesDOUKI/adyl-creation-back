package ld.domain.features.order.reject;

import ld.domain.features.order.lifecycle.AbstractOrderLifecycleUseCase;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.time.Instant;

public class RejectOrderUseCaseImpl extends AbstractOrderLifecycleUseCase
        implements RejectOrderUseCase {

    private final Clock clock;

    public RejectOrderUseCaseImpl(
            OrderEditor orderEditor,
            AggregateEventDispatcher<OrderEvent> orderEventDispatcher,
            UnitOfWork unitOfWork,
            Clock clock) {
        super(orderEditor, orderEventDispatcher, unitOfWork);
        this.clock = clock;
    }

    @Override
    public Result<OrderSnapshot> execute(RejectOrderCommand command) {
        return executeTransition(command.orderId(),
                order -> order.reject(command.reason(), Instant.now(clock)));
    }
}