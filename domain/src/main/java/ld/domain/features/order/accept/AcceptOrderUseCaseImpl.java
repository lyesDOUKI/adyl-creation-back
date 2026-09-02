package ld.domain.features.order.accept;

import ld.domain.features.order.lifecycle.AbstractOrderLifecycleUseCase;
import ld.domain.features.order.lifecycle.DiscountClaimer;
import ld.domain.features.order.lifecycle.DiscountProvider;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.*;
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
        AppliedDiscount discount = resolveDiscount(order);
        return order.accept(discount, Instant.now(clock));
    }

    private AppliedDiscount resolveDiscount(Order order) {
        if (!isEligibleForFirstOrderDiscount(order)) {
            return AppliedDiscount.none();
        }
        return discountProvider.provide(DiscountType.FIRST_ACCEPTED_ORDER)
                .filter(_ -> discountClaimer.tryAddClaim(
                        DiscountType.FIRST_ACCEPTED_ORDER,
                        order.customerEmail()
                ))
                .map(AppliedDiscount::claimed)
                .orElseGet(AppliedDiscount::none);
    }

    private boolean isEligibleForFirstOrderDiscount(Order order) {
        return !customerOrderHistoryFinder.hasEffectiveOrder(order.customerEmail());
    }
}