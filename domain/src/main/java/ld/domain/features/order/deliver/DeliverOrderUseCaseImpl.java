package ld.domain.features.order.deliver;

import ld.domain.features.order.lifecycle.AbstractOrderLifecycleUseCase;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.time.Instant;

public class DeliverOrderUseCaseImpl extends AbstractOrderLifecycleUseCase implements DeliverOrderUseCase {

    private final Clock clock;
    public DeliverOrderUseCaseImpl(OrderEditor orderEditor,
                                      AggregateEventDispatcher<OrderEvent> orderEventDispatcher,
                                      UnitOfWork unitOfWork, Clock clock) {
        super(orderEditor, orderEventDispatcher, unitOfWork);
        this.clock = clock;
    }

    @Override
    public Result<OrderSnapshot> execute(DeliverOrderCommand deliverOrderCommand) {
        return executeOperation(deliverOrderCommand.orderId(),
                order -> order.deliver(deliverOrderCommand.observation(),
                        deliverOrderCommand.deliveryMethod(),Instant.now(clock))
        );
    }
}
