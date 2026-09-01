package ld.application.tests.write.order;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.OrderIntegrationTest;
import ld.application.infra.db.jpa.adapter.OrderCreatorJpaAdapter;
import ld.application.shared.order.CreateOrderCommandFixture;
import ld.application.shared.product.ProductTestFixture;
import ld.domain.features.order.CreateOrderUseCase;
import ld.domain.features.order.OrderCreator;
import ld.domain.features.order.model.OrderCreated;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.helper.test.ResultTestSupport;
import ld.standard.lib.validation.Result;
import org.jooq.DSLContext;
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

import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.Orders.ORDERS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

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

    @MockitoBean
    private AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(ORDER_DETAILS).execute();
        dsl.deleteFrom(ORDERS).execute();
        dsl.deleteFrom(PRODUCTS).execute();
    }

    @Test
    void should_persist_order_and_dispatch_event_when_creation_succeeds() {
        var product = productTestFixture.createExistingProduct();
        var command = CreateOrderCommandFixture.aValidCommand(product.id());

        Result<OrderSnapshot> result = createOrderUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();

        int orderCount = dsl.fetchCount(ORDERS, ORDERS.ID.eq(ResultTestSupport.extractValue(result).orderId()));
        assertThat(orderCount).isEqualTo(1);

        int itemCount = dsl.fetchCount(ORDER_DETAILS, ORDER_DETAILS.ORDER_ID.eq(ResultTestSupport.extractValue(result).orderId()));
        assertThat(itemCount).isEqualTo(1);

        verify(aggregateEventDispatcher, times(1))
                .dispatch(argThat(event -> event instanceof OrderCreated));
    }

    @Test
    void should_not_call_repository_when_product_does_not_exist() {
        var command = CreateOrderCommandFixture.aValidCommandWithUnknownProduct();

        Result<OrderSnapshot> result = createOrderUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(dsl.fetchCount(ORDERS)).isZero();

        verifyNoInteractions(aggregateEventDispatcher);
    }

    @Nested
    @Import(RollbackScenario.FailingRepositoryConfig.class)
    class RollbackScenario {

        @Test
        void should_rollback_order_creation_when_persistence_fails_after_insert() {
            var product = productTestFixture.createExistingProduct();
            var command = CreateOrderCommandFixture.aValidCommand(product.id());

            assertThatThrownBy(() -> createOrderUseCase.execute(command))
                    .isInstanceOf(RuntimeException.class);
            assertThat(dsl.fetchCount(ORDERS)).isZero();
            verifyNoInteractions(aggregateEventDispatcher);
        }

        @TestConfiguration
        static class FailingRepositoryConfig {

            @Bean
            @Primary
            OrderCreator failingCreateOrderRepository(OrderCreatorJpaAdapter realRepository) {
                return snapshot -> {
                    realRepository.create(snapshot);
                    throw new RuntimeException("Simulated failure after insert");
                };
            }
        }
    }
}