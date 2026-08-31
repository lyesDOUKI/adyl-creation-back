package ld.application.bridge;

import ld.domain.features.order.OrderCreator;
import ld.domain.features.order.CreateOrderUseCase;
import ld.domain.features.order.CreateOrderUseCaseImpl;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.product.GetProductRepository;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfiguration {

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderCreator orderCreator,
            GetProductRepository getProductRepository,
            AggregateEventDispatcher<OrderEvent> orderEventDispatcher,
            UnitOfWork unitOfWork
    ) {
        return new CreateOrderUseCaseImpl(orderCreator, getProductRepository,
                orderEventDispatcher, unitOfWork);
    }

    @Bean
    AggregateEventDispatcher<OrderEvent> orderEventDispatcher() {
        return new InMemoryAggregateEventDispatcher<>();
    }
}
