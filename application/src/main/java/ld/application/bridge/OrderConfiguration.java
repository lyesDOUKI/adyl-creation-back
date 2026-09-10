package ld.application.bridge;

import ld.domain.features.order.CreateOrderUseCase;
import ld.domain.features.order.CreateOrderUseCaseImpl;
import ld.domain.features.order.OrderCreator;
import ld.domain.features.order.accept.AcceptOrderUseCase;
import ld.domain.features.order.accept.AcceptOrderUseCaseImpl;
import ld.domain.features.order.accept.CustomerOrderHistoryFinder;
import ld.domain.features.order.deliver.DeliverOrderUseCase;
import ld.domain.features.order.deliver.DeliverOrderUseCaseImpl;
import ld.domain.features.order.lifecycle.DiscountClaimer;
import ld.domain.features.order.lifecycle.DiscountProvider;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.reject.RejectOrderUseCase;
import ld.domain.features.order.reject.RejectOrderUseCaseImpl;
import ld.domain.features.product.ProductFinder;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class OrderConfiguration {

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderCreator orderCreator,
            ProductFinder productFinder,
            AggregateEventDispatcher<OrderEvent> orderEventDispatcher,
            UnitOfWork unitOfWork,
            Clock clock
    ) {
        return new CreateOrderUseCaseImpl(orderCreator, productFinder,
                orderEventDispatcher, unitOfWork, clock);
    }

    @Bean
    public AcceptOrderUseCase acceptOrderUseCase(
            OrderEditor orderEditor,
            CustomerOrderHistoryFinder customerOrderHistoryFinder,
            DiscountClaimer discountClaimer,
            DiscountProvider discountProvider,
            AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher,
            UnitOfWork unitOfWork,
            Clock clock) {
        return new AcceptOrderUseCaseImpl(
                orderEditor,
                customerOrderHistoryFinder,
                discountClaimer,
                discountProvider,
                orderEventAggregateEventDispatcher,
                unitOfWork,
                clock
        );
    }

    @Bean
    public RejectOrderUseCase rejectOrderUseCase(
            OrderEditor orderEditor,
            AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher,
            UnitOfWork unitOfWork,
            Clock clock
    ) {
        return new RejectOrderUseCaseImpl(orderEditor, orderEventAggregateEventDispatcher, unitOfWork, clock);
    }

    @Bean
    public DeliverOrderUseCase deliverOrderUseCase(
            OrderEditor orderEditor,
            AggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher,
            UnitOfWork unitOfWork,
            Clock clock
    ) {
        return new DeliverOrderUseCaseImpl(orderEditor, orderEventAggregateEventDispatcher, unitOfWork, clock);
    }
}
