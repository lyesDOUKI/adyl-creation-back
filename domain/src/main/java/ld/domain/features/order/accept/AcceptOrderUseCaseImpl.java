package ld.domain.features.order.accept;

import ld.domain.features.order.lifecycle.DiscountClaimer;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.DiscountType;
import ld.domain.features.order.model.Order;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.time.Instant;

public class AcceptOrderUseCaseImpl implements AcceptOrderUseCase {

    private final OrderEditor orderEditor;
    private final DiscountClaimer discountClaimer;
    private final AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public AcceptOrderUseCaseImpl(OrderEditor orderEditor,
                                  DiscountClaimer discountClaimer,
                                  AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher,
                                  UnitOfWork unitOfWork,
                                  Clock clock) {
        this.orderEditor = orderEditor;
        this.discountClaimer = discountClaimer;
        this.orderEventAggregateEventDispatcher = orderEventAggregateEventDispatcher;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    @Override
    public Result<OrderSnapshot> execute(AcceptOrderCommand acceptOrderCommand) {
        Result<Order> orderResult = unitOfWork.executeInTransaction(() ->
                this.orderEditor.findById(acceptOrderCommand.orderId())
                        .map(Result::success)
                        .orElseGet(() -> Result.resourceNotFound(OrderErrorCode.ORDER_NOT_FOUND, "Commande introuvable",
                                String.format("La commande %s est introuvable", acceptOrderCommand.orderId())))
                        .map(Order::from)
                        .flatMap(order -> {
                            boolean isFirstAcceptedOrder = this.discountClaimer.tryAddClaim(
                                    DiscountType.FIRST_ACCEPTED_ORDER,
                                    order.customerEmail()
                            );
                            return order.accept(isFirstAcceptedOrder, Instant.now(clock));
                        })
                        .flatMap(order -> {
                            this.orderEditor.save(order.toSnapshot());
                            return Result.success(order);
                        })
        );

        return orderResult.map(order -> {
            order.getDomainEvents().forEach(this.orderEventAggregateEventDispatcher::dispatch);
            return order.toSnapshot();
        });
    }
}