package ld.domain.features.order.accept;

import ld.domain.features.order.lifecycle.DiscountClaimer;
import ld.domain.features.order.lifecycle.DiscountProvider;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.DiscountType;
import ld.domain.features.order.model.Order;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.valueObjects.Percentage;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class AcceptOrderUseCaseImpl implements AcceptOrderUseCase {
    private final OrderEditor orderEditor;
    private final CustomerOrderHistoryFinder customerOrderHistoryFinder;
    private final DiscountClaimer discountClaimer;
    private final DiscountProvider discountProvider;
    private final AggregateEventDispatcher<OrderEvent> orderEventDispatcher;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public AcceptOrderUseCaseImpl(
            OrderEditor orderEditor,
            CustomerOrderHistoryFinder customerOrderHistoryFinder,
            DiscountClaimer discountClaimer, DiscountProvider discountProvider,
            AggregateEventDispatcher<OrderEvent> orderEventDispatcher,
            UnitOfWork unitOfWork,
            Clock clock) {
        this.orderEditor = orderEditor;
        this.customerOrderHistoryFinder = customerOrderHistoryFinder;
        this.discountClaimer = discountClaimer;
        this.discountProvider = discountProvider;
        this.orderEventDispatcher = orderEventDispatcher;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    @Override
    public Result<OrderSnapshot> execute(AcceptOrderCommand command) {
        Result<Order> result = unitOfWork.executeInTransaction(
                () -> acceptOrder(command)
        );
        return result
                .map(this::dispatchEvents)
                .map(Order::toSnapshot);
    }

    private Result<Order> acceptOrder(AcceptOrderCommand command) {
        return findOrder(command.orderId())
                .map(Order::from)
                .flatMap(this::acceptWithDiscountHandling)
                .map(this::save);
    }

    private Result<OrderSnapshot> findOrder(UUID orderId) {
        return orderEditor.findById(orderId)
                .map(Result::success)
                .orElseGet(() -> Result.resourceNotFound(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "Commande introuvable",
                        String.format("La commande %s est introuvable", orderId)
                ));
    }

    private Result<Order> acceptWithDiscountHandling(Order order) {
        boolean eligibleForFirstOrderDiscount = isEligibleForFirstOrderDiscount(order);

        if (!eligibleForFirstOrderDiscount) {
            return order.accept(false, Percentage.ZERO, Instant.now(clock));
        }

        boolean claimed = discountClaimer.tryAddClaim(
                DiscountType.FIRST_ACCEPTED_ORDER,
                order.customerEmail()
        );

        return order.accept(claimed,
                this.discountProvider.provide(DiscountType.FIRST_ACCEPTED_ORDER).orElse(Percentage.ZERO)
                , Instant.now(clock));
    }

    private boolean isEligibleForFirstOrderDiscount(Order order) {
        return !customerOrderHistoryFinder.hasEffectiveOrder(order.customerEmail());
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