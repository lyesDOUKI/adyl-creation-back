package ld.application.tests.write.product;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.ProductIntegrationTest;
import ld.application.shared.product.CreateProductCommandFixture;
import ld.application.shared.product.ProductTestFixture;
import ld.domain.features.product.CreateProductUseCase;
import ld.domain.features.product.ProductCreator;
import ld.domain.features.product.model.ProductCreated;
import ld.domain.features.product.model.ProductEvent;
import ld.domain.features.product.model.ProductSnapshot;
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

import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ProductIntegrationTest
class CreateProductServiceIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private CreateProductUseCase createProductUseCase;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ProductTestFixture productTestFixture;

    @MockitoBean
    private AggregateEventDispatcher<ProductEvent> aggregateEventDispatcher;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(PRODUCT_COLORS).execute();
        dsl.deleteFrom(PRODUCT_PHOTOS).execute();
        dsl.deleteFrom(PRODUCTS).execute();
    }

    @Test
    void should_persist_product_and_dispatch_event_when_creation_succeeds() {
        var command = CreateProductCommandFixture.aValidCommand();

        Result<ProductSnapshot> result = createProductUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();
        var snapshot = ResultTestSupport.extractValue(result);

        int productCount = dsl.fetchCount(PRODUCTS, PRODUCTS.ID.eq(snapshot.productId()));
        assertThat(productCount).isEqualTo(1);

        int colorCount = dsl.fetchCount(PRODUCT_COLORS, PRODUCT_COLORS.PRODUCT_ID.eq(snapshot.productId()));
        assertThat(colorCount).isEqualTo(command.colors().size());

        verify(aggregateEventDispatcher, times(1))
                .dispatch(argThat(event -> event instanceof ProductCreated));
    }

    @Test
    void should_not_persist_nor_dispatch_when_product_name_already_exists() {
        var existingProduct = productTestFixture.createExistingProduct();
        var command = CreateProductCommandFixture.aCommandWithName(existingProduct.name());

        Result<ProductSnapshot> result = createProductUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(dsl.fetchCount(PRODUCTS)).isEqualTo(1);
        verifyNoInteractions(aggregateEventDispatcher);
    }

    @Nested
    @Import(RollbackScenario.FailingProductCreatorConfig.class)
    class RollbackScenario {

        @Test
        void should_rollback_product_creation_when_persistence_fails_after_insert() {
            var command = CreateProductCommandFixture.aValidCommand();

            assertThatThrownBy(() -> createProductUseCase.execute(command))
                    .isInstanceOf(RuntimeException.class);

            assertThat(dsl.fetchCount(PRODUCTS)).isZero();
            assertThat(dsl.fetchCount(PRODUCT_COLORS)).isZero();
            verifyNoInteractions(aggregateEventDispatcher);
        }

        @TestConfiguration
        static class FailingProductCreatorConfig {

            @Bean
            @Primary
            ProductCreator failingProductCreator(ProductCreator productCreator) {
                return snapshot -> {
                    productCreator.create(snapshot);
                    throw new RuntimeException("Simulated failure after insert");
                };
            }
        }
    }
}
