package ld.application.tests.write.order;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.OrderIntegrationTest;
import ld.application.infra.db.converter.OrderStatusConverter;
import ld.application.infra.db.jpa.adapter.OrderEditorJpaAdapter;
import ld.application.shared.product.ProductTestFixture;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.*;
import ld.domain.features.order.reject.RejectOrderCommand;
import ld.domain.features.order.reject.RejectOrderUseCase;
import ld.domain.valueObjects.Percentage;
import ld.standard.lib.AggregateEventDispatcher;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static ld.application.jooq.tables.Customers.CUSTOMERS;
import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.Orders.ORDERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@OrderIntegrationTest
class RejectOrderServiceIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private RejectOrderUseCase rejectOrderUseCase;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ProductTestFixture productTestFixture;

    @Autowired
    private Clock clock;

    @MockitoBean
    private AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher;

    private final OrderStatusConverter converter = new OrderStatusConverter();

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(ORDER_DETAILS).execute();
        dsl.deleteFrom(ORDERS).execute();
        dsl.deleteFrom(CUSTOMERS).execute();
    }

    @Test
    void should_reject_pending_order_and_dispatch_event() {
        var product = productTestFixture.createExistingProduct();
        UUID orderId = UUID.randomUUID();
        String customerEmail = "jean.dupont@example.com";
        String reason = "Produit indisponible";

        insertPendingOrderInDb(orderId, customerEmail, product.id());

        var command = new RejectOrderCommand(orderId, reason);

        var result = rejectOrderUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();

        var orderRecord = dsl.fetchOne(ORDERS, ORDERS.ID.eq(orderId));

        assertThat(orderRecord).isNotNull();
        assertThat(orderRecord.getStatusType())
                .isEqualTo(OrderState.REJECTED.name());

        var rejectedStatus = converter.convertToEntityAttribute(
                orderRecord.getStatusData().data()
        );

        assertThat(rejectedStatus)
                .isInstanceOf(OrderStatus.Rejected.class);

        var rejected = (OrderStatus.Rejected) rejectedStatus;

        assertThat(rejected.reason())
                .isEqualTo(reason);

        assertThat(rejected.rejectedAt())
                .isNotNull();

        verify(aggregateEventDispatcher, times(1))
                .dispatch(argThat(event -> event instanceof OrderRejected));
    }

    @Test
    void should_reject_accepted_order_and_dispatch_event() {
        var product = productTestFixture.createExistingProduct();
        UUID orderId = UUID.randomUUID();
        String customerEmail = "accepted.user@example.com";
        String reason = "Commande annulée";

        insertAcceptedOrderInDb(orderId, customerEmail, product.id());

        var command = new RejectOrderCommand(orderId, reason);

        var result = rejectOrderUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();

        var orderRecord = dsl.fetchOne(ORDERS, ORDERS.ID.eq(orderId));

        assertThat(orderRecord).isNotNull();
        assertThat(orderRecord.getStatusType())
                .isEqualTo(OrderState.REJECTED.name());

        var rejectedStatus = converter.convertToEntityAttribute(
                orderRecord.getStatusData().data()
        );

        assertThat(rejectedStatus)
                .isInstanceOf(OrderStatus.Rejected.class);

        var rejected = (OrderStatus.Rejected) rejectedStatus;

        assertThat(rejected.reason())
                .isEqualTo(reason);

        verify(aggregateEventDispatcher, times(1))
                .dispatch(argThat(event -> event instanceof OrderRejected));
    }

    @Test
    void should_return_not_found_and_not_dispatch_event_when_order_does_not_exist() {
        UUID unknownOrderId = UUID.randomUUID();

        var command = new RejectOrderCommand(
                unknownOrderId,
                "Produit indisponible"
        );

        var result = rejectOrderUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();

        verifyNoInteractions(aggregateEventDispatcher);
    }

    @Test
    void should_not_reject_delivered_order_and_not_dispatch_event() {
        var product = productTestFixture.createExistingProduct();
        UUID orderId = UUID.randomUUID();
        String customerEmail = "delivered.user@example.com";

        insertDeliveredOrderInDb(
                orderId,
                customerEmail,
                product.id()
        );

        var command = new RejectOrderCommand(
                orderId,
                "Produit indisponible"
        );

        var result = rejectOrderUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();

        var orderRecord = dsl.fetchOne(ORDERS, ORDERS.ID.eq(orderId));

        assertThat(orderRecord).isNotNull();
        assertThat(orderRecord.getStatusType())
                .isEqualTo(OrderState.DELIVERED.name());

        verifyNoInteractions(aggregateEventDispatcher);
    }

    @Nested
    @Import(RollbackScenario.FailingRepositoryConfig.class)
    class RollbackScenario {

        @Test
        void should_rollback_order_rejection_when_persistence_fails() {
            var product = productTestFixture.createExistingProduct();
            UUID orderId = UUID.randomUUID();
            String customerEmail = "rollback.user@example.com";

            insertPendingOrderInDb(
                    orderId,
                    customerEmail,
                    product.id()
            );

            var command = new RejectOrderCommand(
                    orderId,
                    "Produit indisponible"
            );

            assertThatThrownBy(
                    () -> rejectOrderUseCase.execute(command)
            ).isInstanceOf(RuntimeException.class);

            var orderRecord = dsl.fetchOne(
                    ORDERS,
                    ORDERS.ID.eq(orderId)
            );

            assertThat(orderRecord).isNotNull();
            assertThat(orderRecord.getStatusType())
                    .isEqualTo(OrderState.PENDING.name());

            verifyNoInteractions(aggregateEventDispatcher);
        }

        @TestConfiguration
        static class FailingRepositoryConfig {

            @Bean
            @Primary
            OrderEditor failingOrderEditor(
                    OrderEditorJpaAdapter realRepository
            ) {
                return new OrderEditor() {

                    @Override
                    public Optional<OrderSnapshot> findById(UUID id) {
                        return realRepository.findById(id);
                    }

                    @Override
                    public void save(OrderSnapshot snapshot) {
                        realRepository.save(snapshot);
                        throw new RuntimeException(
                                "Simulated failure during save in UnitOfWork"
                        );
                    }
                };
            }
        }
    }

    private UUID insertCustomerInDb(String customerEmail) {
        UUID customerId = UUID.randomUUID();

        dsl.insertInto(CUSTOMERS)
                .set(CUSTOMERS.ID, customerId)
                .set(CUSTOMERS.CUSTOMER_NAME, "Jean Dupont")
                .set(CUSTOMERS.CUSTOMER_EMAIL, customerEmail)
                .set(CUSTOMERS.CUSTOMER_PHONE, "0600000000")
                .execute();

        return customerId;
    }

    private void insertPendingOrderInDb(
            UUID orderId,
            String customerEmail,
            UUID productId
    ) {
        insertOrderInDb(
                orderId,
                customerEmail,
                productId,
                OrderState.PENDING,
                OrderStatus.PENDING
        );
    }

    private void insertAcceptedOrderInDb(
            UUID orderId,
            String customerEmail,
            UUID productId
    ) {
        insertOrderInDb(
                orderId,
                customerEmail,
                productId,
                OrderState.ACCEPTED,
                new OrderStatus.Accepted(
                        Instant.now(clock),
                        Percentage.ZERO
                )
        );
    }

    private void insertDeliveredOrderInDb(
            UUID orderId,
            String customerEmail,
            UUID productId
    ) {
        insertOrderInDb(
                orderId,
                customerEmail,
                productId,
                OrderState.DELIVERED,
                new OrderStatus.Delivered("Bonne commande",
                        DeliveryMethod.HAND_DELIVERY, Instant.now(clock))
        );
    }

    private void insertOrderInDb(
            UUID orderId,
            String customerEmail,
            UUID productId,
            OrderState state,
            OrderStatus status
    ) {
        UUID customerId = insertCustomerInDb(customerEmail);

        dsl.insertInto(ORDERS)
                .set(ORDERS.ID, orderId)
                .set(ORDERS.CUSTOMER_ID, customerId)
                .set(ORDERS.STATUS_TYPE, state.name())
                .set(
                        ORDERS.STATUS_DATA,
                        JSON.json(
                                converter.convertToDatabaseColumn(status)
                        )
                )
                .set(ORDERS.TOTAL, new BigDecimal("100.00"))
                .set(ORDERS.CREATED_AT, LocalDateTime.now())
                .set(ORDERS.UPDATED_AT, LocalDateTime.now())
                .execute();

        dsl.insertInto(ORDER_DETAILS)
                .set(ORDER_DETAILS.ID, UUID.randomUUID())
                .set(ORDER_DETAILS.ORDER_ID, orderId)
                .set(ORDER_DETAILS.PRODUCT_ID, productId)
                .set(ORDER_DETAILS.QUANTITY, new BigDecimal("1.00"))
                .set(ORDER_DETAILS.UNIT_PRICE, new BigDecimal("100.00"))
                .set(ORDER_DETAILS.TOTAL_AMOUNT, new BigDecimal("100.00"))
                .set(ORDER_DETAILS.DISCOUNT_RATE, BigDecimal.ZERO)
                .execute();
    }
}
