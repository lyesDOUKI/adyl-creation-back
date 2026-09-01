package ld.domain.features.order.accept;

import ld.domain.features.order.lifecycle.AbstractOrderLifecycleUseCase;
import ld.domain.features.order.lifecycle.DiscountClaimer;
import ld.domain.features.order.lifecycle.DiscountProvider;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.DiscountType;
import ld.domain.features.order.model.Order;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.valueObjects.Percentage;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.time.Instant;

public class AcceptOrderUseCaseImpl extends AbstractOrderLifecycleUseCase
        implements AcceptOrderUseCase {

    private final CustomerOrderHistoryFinder customerOrderHistoryFinder;
    private final DiscountClaimer discountClaimer;
    private final DiscountProvider discountProvider;
    private final Clock clock;

    public AcceptOrderUseCaseImpl(
            OrderEditor orderEditor,
            CustomerOrderHistoryFinder customerOrderHistoryFinder,
            DiscountClaimer discountClaimer,
            DiscountProvider discountProvider,
            AggregateEventDispatcher<OrderEvent> orderEventDispatcher,
            UnitOfWork unitOfWork,
            Clock clock) {
        super(orderEditor, orderEventDispatcher, unitOfWork);
        this.customerOrderHistoryFinder = customerOrderHistoryFinder;
        this.discountClaimer = discountClaimer;
        this.discountProvider = discountProvider;
        this.clock = clock;
    }

    @Override
    public Result<OrderSnapshot> execute(AcceptOrderCommand command) {
        return executeOperation(command.orderId(), this::acceptWithDiscountHandling);
    }

    private Result<Order> acceptWithDiscountHandling(Order order) {
        boolean eligibleForFirstOrderDiscount = isEligibleForFirstOrderDiscount(order);

        if (!eligibleForFirstOrderDiscount) {
            return order.accept(false, Percentage.ZERO, Instant.now(clock));
        }

        var rate = discountProvider.provide(DiscountType.FIRST_ACCEPTED_ORDER);

        boolean claimed = rate
                .map(_ -> discountClaimer.tryAddClaim(
                        DiscountType.FIRST_ACCEPTED_ORDER,
                        order.customerEmail()
                ))
                .orElse(false);

        return order.accept(
                claimed,
                rate.orElse(Percentage.ZERO),
                Instant.now(clock)
        );
    }

    private boolean isEligibleForFirstOrderDiscount(Order order) {
        return !customerOrderHistoryFinder.hasEffectiveOrder(order.customerEmail());
    }
}