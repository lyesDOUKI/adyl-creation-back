package ld.application.tests.write.order;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.OrderIntegrationTest;
import ld.application.infra.db.jpa.adapter.OrderEditorJpaAdapter;
import ld.application.infra.db.converter.OrderStatusConverter;
import ld.application.shared.product.ProductTestFixture;
import ld.domain.features.order.accept.AcceptOrderCommand;
import ld.domain.features.order.accept.AcceptOrderUseCase;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.*;
import ld.domain.valueObjects.Percentage;
import ld.standard.lib.AggregateEventDispatcher;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static ld.application.jooq.tables.Customers.CUSTOMERS;
import static ld.application.jooq.tables.DiscountClaims.DISCOUNT_CLAIMS;
import static ld.application.jooq.tables.DiscountRates.DISCOUNT_RATES;
import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.Orders.ORDERS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@OrderIntegrationTest
class AcceptOrderServiceIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private AcceptOrderUseCase acceptOrderUseCase;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ProductTestFixture productTestFixture;

    @MockitoBean
    private AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher;

    private final OrderStatusConverter converter = new OrderStatusConverter();
    @BeforeEach
    void setUp() {
        dsl.deleteFrom(DISCOUNT_CLAIMS).execute();
        dsl.deleteFrom(ORDER_DETAILS).execute();
        dsl.deleteFrom(ORDERS).execute();
        dsl.deleteFrom(CUSTOMERS).execute();
        dsl.deleteFrom(PRODUCTS).execute();
        configureDiscountRate();
    }

    private void configureDiscountRate() {
        dsl.deleteFrom(DISCOUNT_RATES).execute();
        dsl.insertInto(DISCOUNT_RATES)
                .set(DISCOUNT_RATES.ID, UUID.randomUUID())
                .set(
                        DISCOUNT_RATES.DISCOUNT_TYPE,
                        DiscountType.FIRST_ACCEPTED_ORDER.name()
                )
                .set(DISCOUNT_RATES.RATE, BigDecimal.TEN)
                .set(DISCOUNT_RATES.CREATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
                .execute();
    }
    @Test
    void should_accept_first_order_apply_discount_and_dispatch_event() {
        var product = productTestFixture.createExistingProduct();
        UUID orderId = UUID.randomUUID();
        String customerEmail = "jean.dupont@example.com";
        insertPendingOrderInDb(orderId, customerEmail, product.id());

        var command = new AcceptOrderCommand(orderId);

        var result = acceptOrderUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();

        var orderRecord = dsl.fetchOne(ORDERS, ORDERS.ID.eq(orderId));
        assertThat(orderRecord).isNotNull();
        assertThat(orderRecord.getStatusType()).isEqualTo(OrderState.ACCEPTED.name());

        var detailRecord = dsl.fetchOne(ORDER_DETAILS, ORDER_DETAILS.ORDER_ID.eq(orderId));
        assertThat(detailRecord).isNotNull();
        assertThat(detailRecord.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.1000"));

        int claimCount = dsl.fetchCount(DISCOUNT_CLAIMS, DISCOUNT_CLAIMS.EMAIL.eq(customerEmail));
        assertThat(claimCount).isEqualTo(1);

        verify(aggregateEventDispatcher, times(1))
                .dispatch(argThat(event -> event instanceof OrderAccepted));
    }

    @Test
    void should_accept_order_without_discount_when_customer_already_has_effective_order() {
        String customerEmail = "client.fidele@example.com";
        var product = productTestFixture.createExistingProduct();
        insertAcceptedOrderInDb(UUID.randomUUID(), customerEmail, product.id());

        UUID newOrderId = UUID.randomUUID();
        insertPendingOrderInDb(newOrderId, customerEmail, product.id());

        var command = new AcceptOrderCommand(newOrderId);

        var result = acceptOrderUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();

        var orderRecord = dsl.fetchOne(ORDERS, ORDERS.ID.eq(newOrderId));
        assertThat(orderRecord).isNotNull();
        assertThat(orderRecord.getStatusType()).isEqualTo(OrderState.ACCEPTED.name());

        var detailRecord = dsl.fetchOne(ORDER_DETAILS, ORDER_DETAILS.ORDER_ID.eq(newOrderId));
        assertThat(detailRecord).isNotNull();
        assertThat(detailRecord.getDiscountRate()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(aggregateEventDispatcher, times(1))
                .dispatch(argThat(event -> event instanceof OrderAccepted));
    }

    @Test
    void should_return_not_found_and_not_dispatch_event_when_order_does_not_exist() {
        UUID unknownOrderId = UUID.randomUUID();
        var command = new AcceptOrderCommand(unknownOrderId);

        var result = acceptOrderUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();

        verifyNoInteractions(aggregateEventDispatcher);
    }

    @Nested
    @Import(RollbackScenario.FailingRepositoryConfig.class)
    class RollbackScenario {

        @Test
        void should_rollback_order_acceptance_and_discount_claim_when_persistence_fails() {
            var product = productTestFixture.createExistingProduct();
            UUID orderId = UUID.randomUUID();
            String customerEmail = "rollback.user@example.com";
            insertPendingOrderInDb(orderId, customerEmail, product.id());

            var command = new AcceptOrderCommand(orderId);

            assertThatThrownBy(() -> acceptOrderUseCase.execute(command))
                    .isInstanceOf(RuntimeException.class);

            var orderRecord = dsl.fetchOne(ORDERS, ORDERS.ID.eq(orderId));
            assertThat(orderRecord).isNotNull();
            assertThat(orderRecord.getStatusType()).isEqualTo(OrderState.PENDING.name());

            int claimCount = dsl.fetchCount(DISCOUNT_CLAIMS, DISCOUNT_CLAIMS.EMAIL.eq(customerEmail));
            assertThat(claimCount).isZero();

            verifyNoInteractions(aggregateEventDispatcher);
        }

        @TestConfiguration
        static class FailingRepositoryConfig {

            @Bean
            @Primary
            OrderEditor failingOrderEditor(OrderEditorJpaAdapter realRepository) {
                return new OrderEditor() {
                    @Override
                    public Optional<OrderSnapshot> findById(UUID id) {
                        return realRepository.findById(id);
                    }

                    @Override
                    public void save(OrderSnapshot snapshot) {
                        realRepository.save(snapshot);
                        throw new RuntimeException("Simulated failure during save in UnitOfWork");
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

    private void insertPendingOrderInDb(UUID orderId, String customerEmail, UUID productId) {
        UUID customerId = insertCustomerInDb(customerEmail);
        dsl.insertInto(ORDERS)
                .set(ORDERS.ID, orderId)
                .set(ORDERS.CUSTOMER_ID, customerId)
                .set(ORDERS.STATUS_TYPE, OrderState.PENDING.name())
                .set(
                        ORDERS.STATUS_DATA,
                        JSON.json(converter.convertToDatabaseColumn(OrderStatus.PENDING))
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

    private void insertAcceptedOrderInDb(UUID orderId, String customerEmail, UUID productId) {
        UUID customerId = insertCustomerInDb(customerEmail);

        dsl.insertInto(ORDERS)
                .set(ORDERS.ID, orderId)
                .set(ORDERS.CUSTOMER_ID, customerId)
                .set(ORDERS.STATUS_TYPE, OrderState.ACCEPTED.name())
                .set(
                        ORDERS.STATUS_DATA,
                        JSON.json(converter.convertToDatabaseColumn(new OrderStatus.Accepted(Instant.now(), Percentage.ZERO)))
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

