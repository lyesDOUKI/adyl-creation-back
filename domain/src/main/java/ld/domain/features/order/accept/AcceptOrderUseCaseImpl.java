package ld.domain.features.order.accept;

import ld.domain.features.order.lifecycle.DiscountClaimRepository;
import ld.domain.features.order.lifecycle.OrderLifecycleRepository;
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

    private final OrderLifecycleRepository orderLifecycleRepository;
    private final DiscountClaimRepository discountClaimRepository;
    private final AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public AcceptOrderUseCaseImpl(OrderLifecycleRepository orderLifecycleRepository,
                                  DiscountClaimRepository discountClaimRepository,
                                  AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher,
                                  UnitOfWork unitOfWork,
                                  Clock clock) {
        this.orderLifecycleRepository = orderLifecycleRepository;
        this.discountClaimRepository = discountClaimRepository;
        this.orderEventAggregateEventDispatcher = orderEventAggregateEventDispatcher;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    @Override
    public Result<OrderSnapshot> execute(AcceptOrderCommand acceptOrderCommand) {
        Result<Order> orderResult = unitOfWork.executeInTransaction(() ->
                this.orderLifecycleRepository.findById(acceptOrderCommand.orderId())
                        .map(Result::success)
                        .orElseGet(() -> Result.resourceNotFound(OrderErrorCode.ORDER_NOT_FOUND, "Commande introuvable",
                                String.format("La commande %s est introuvable", acceptOrderCommand.orderId())))
                        .map(Order::from)
                        .flatMap(order -> {
                            boolean isFirstAcceptedOrder = this.discountClaimRepository.tryClaim(
                                    DiscountType.FIRST_ACCEPTED_ORDER,
                                    order.customerEmail()
                            );
                            return order.accept(isFirstAcceptedOrder, Instant.now(clock));
                        })
                        .flatMap(order -> {
                            this.orderLifecycleRepository.save(order.toSnapshot());
                            return Result.success(order);
                        })
        );

        return orderResult.map(order -> {
            order.getDomainEvents().forEach(this.orderEventAggregateEventDispatcher::dispatch);
            return order.toSnapshot();
        });
    }
}