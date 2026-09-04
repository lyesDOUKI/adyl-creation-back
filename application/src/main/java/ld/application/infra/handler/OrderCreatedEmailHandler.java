package ld.application.infra.handler;

import ld.application.infra.email.core.EmailSender;
import ld.application.infra.email.order.OrderCreatedEmail;
import ld.domain.features.order.model.OrderCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEmailHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCreatedEmailHandler.class);

    private final OrderCreatedEmail orderCreatedEmail;
    private final EmailSender emailSender;

    public OrderCreatedEmailHandler(
            OrderCreatedEmail orderCreatedEmail,
            EmailSender emailSender
    ) {
        this.orderCreatedEmail = orderCreatedEmail;
        this.emailSender = emailSender;
    }

    @Async("emailTaskExecutor")
    @EventListener
    public void handle(OrderCreated event) {
        try {
            var email = orderCreatedEmail.create(event);
            emailSender.send(email);
            LOGGER.info("Email send for order id : {}", event.orderId());
        } catch (Exception e) {
            LOGGER.error("Failed to send order created email for order {}", event.orderId(), e);
        }
    }
}