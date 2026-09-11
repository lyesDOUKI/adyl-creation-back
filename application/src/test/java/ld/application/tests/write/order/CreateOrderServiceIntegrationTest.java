package ld.application.tests.write.order;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.config.order.SwitchableOrderCreator;
import ld.application.context.OrderIntegrationTest;
import ld.application.shared.order.CreateOrderCommandFixture;
import ld.application.shared.product.ProductTestFixture;
import ld.domain.features.order.CreateOrderUseCase;
import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.helper.test.ResultTestSupport;
import ld.standard.lib.validation.Result;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static ld.application.jooq.tables.Customers.CUSTOMERS;
import static ld.application.jooq.tables.OrderDeliveryAddresses.ORDER_DELIVERY_ADDRESSES;
import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.Orders.ORDERS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@OrderIntegrationTest
class CreateOrderServiceIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private CreateOrderUseCase createOrderUseCase;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ProductTestFixture productTestFixture;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(ORDER_DETAILS).execute();
        dsl.deleteFrom(ORDERS).execute();
        dsl.deleteFrom(ORDER_DELIVERY_ADDRESSES).execute();
        dsl.deleteFrom(CUSTOMERS).execute();
        dsl.deleteFrom(PRODUCTS).execute();
    }

    @Test
    void should_persist_order_and_dispatch_event_when_creation_succeeds() {
        UUID customerIdentitySubject = UUID.randomUUID();
        insertCustomerInDb(customerIdentitySubject);

        var product = productTestFixture.createExistingProduct();
        var command = CreateOrderCommandFixture.aValidCommand(
                product.id(),
                customerIdentitySubject
        );

        Result<OrderSnapshot> result = createOrderUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();

        UUID orderId = ResultTestSupport.extractValue(result).orderId();

        int orderCount = dsl.fetchCount(
                ORDERS,
                ORDERS.ID.eq(orderId)
        );
        assertThat(orderCount).isEqualTo(1);

        int itemCount = dsl.fetchCount(
                ORDER_DETAILS,
                ORDER_DETAILS.ORDER_ID.eq(orderId)
        );
        assertThat(itemCount).isEqualTo(1);
    }

    @Test
    void should_not_call_repository_when_product_does_not_exist() {
        var command = CreateOrderCommandFixture.aValidCommandWithUnknownProduct();

        Result<OrderSnapshot> result = createOrderUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(dsl.fetchCount(ORDERS)).isZero();
    }

    @Nested
    class RollbackScenario {

        @Autowired
        SwitchableOrderCreator switchableOrderCreator;
        @Test
        void should_rollback_order_creation_when_persistence_fails_after_insert() {
            switchableOrderCreator.failAfterCreateWith(new RuntimeException("exception after persistence"));
            UUID customerIdentitySubject = UUID.randomUUID();
            insertCustomerInDb(customerIdentitySubject);

            var product = productTestFixture.createExistingProduct();
            var command = CreateOrderCommandFixture.aValidCommand(
                    product.id(),
                    customerIdentitySubject
            );

            assertThatThrownBy(() -> createOrderUseCase.execute(command))
                    .isInstanceOf(RuntimeException.class);

            assertThat(dsl.fetchCount(ORDERS)).isZero();
        }
    }

    private void insertCustomerInDb(UUID identitySubject) {
        dsl.insertInto(CUSTOMERS)
                .set(CUSTOMERS.ID, UUID.randomUUID())
                .set(CUSTOMERS.IDENTITY_SUBJECT, identitySubject)
                .set(CUSTOMERS.EMAIL, "customer@test.com")
                .set(CUSTOMERS.PHONE, "0600000000")
                .execute();
    }
}