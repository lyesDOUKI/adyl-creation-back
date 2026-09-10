package ld.application.infra.email.order;

import ld.application.infra.email.config.EmailProperties;
import ld.application.infra.email.core.AbstractEmailTemplate;
import ld.application.infra.email.core.EmailTemplateRenderer;
import ld.domain.features.order.model.OrderCreated;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ld.application.jooq.Tables.ORDER_DETAILS;
import static ld.application.jooq.Tables.PRODUCTS;
import static ld.application.jooq.tables.Customers.CUSTOMERS;
import static ld.application.jooq.tables.Orders.ORDERS;

@Component
public class OrderCreatedEmail extends AbstractEmailTemplate<OrderCreated, OrderCreatedEmailModel> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy 'à' HH:mm")
            .withZone(ZoneOffset.UTC);

    private final DSLContext dsl;

    public OrderCreatedEmail(
            EmailProperties properties,
            EmailTemplateRenderer renderer,
            DSLContext dsl
    ) {
        super(properties, renderer);
        this.dsl = dsl;
    }

    @Override
    protected String templateName() {
        return "order-created";
    }

    @Override
    protected OrderCreatedEmailModel createModel(OrderCreated event) {
        var orderRecord = dsl.select(
                        ORDERS.CREATED_AT,
                        ORDERS.TOTAL,
                        ORDERS.CUSTOMER_MESSAGE,
                        CUSTOMERS.EMAIL
                )
                .from(ORDERS)
                .join(CUSTOMERS).on(CUSTOMERS.ID.eq(ORDERS.CUSTOMER_ID))
                .where(ORDERS.ID.eq(event.orderId()))
                .fetchOne();

        if (orderRecord == null) {
            throw new IllegalStateException("Order not found: " + event.orderId());
        }

        List<OrderCreatedEmailModel.ItemModel> items = dsl.select(
                        PRODUCTS.NAME,
                        ORDER_DETAILS.QUANTITY,
                        ORDER_DETAILS.UNIT_PRICE,
                        ORDER_DETAILS.TOTAL_AMOUNT,
                        ORDER_DETAILS.CHOSEN_COLOR,
                        ORDER_DETAILS.DISCOUNT_RATE
                )
                .from(ORDER_DETAILS)
                .join(PRODUCTS)
                .on(PRODUCTS.ID.eq(ORDER_DETAILS.PRODUCT_ID))
                .where(ORDER_DETAILS.ORDER_ID.eq(event.orderId()))
                .fetch(r -> new OrderCreatedEmailModel.ItemModel(
                        r.get(PRODUCTS.NAME),
                        r.get(ORDER_DETAILS.QUANTITY),
                        r.get(ORDER_DETAILS.UNIT_PRICE),
                        r.get(ORDER_DETAILS.TOTAL_AMOUNT),
                        r.get(ORDER_DETAILS.CHOSEN_COLOR),
                        r.get(ORDER_DETAILS.DISCOUNT_RATE)
                ));

        return new OrderCreatedEmailModel(
                event.orderId(),
                orderRecord.get(CUSTOMERS.EMAIL),
                orderRecord.get(ORDERS.CUSTOMER_MESSAGE),
                DATE_FORMATTER.format(orderRecord.get(ORDERS.CREATED_AT).toInstant(ZoneOffset.UTC)),
                orderRecord.get(ORDERS.TOTAL),
                items
        );
    }
}