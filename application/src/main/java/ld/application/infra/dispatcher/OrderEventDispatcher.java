package ld.application.infra.dispatcher;

import ld.domain.features.order.model.OrderEvent;
import ld.standard.lib.AggregateEventDispatcher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OrderEventDispatcher implements AggregateEventDispatcher<OrderEvent> {

    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void dispatch(OrderEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
