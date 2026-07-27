package ld.domain.features.product;

import ld.domain.features.product.model.ProductEvent;
import ld.domain.features.product.model.ProductStatus;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.domain.features.shared.ProductSnapshotTestBuilder;
import ld.domain.helper.ResultTestSupport;
import ld.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.lib.validation.FailureType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class CreateProductUseCaseTest {

    InMemoryCreateProductRepository createProductRepository = new InMemoryCreateProductRepository();
    InMemoryAggregateEventDispatcher<ProductEvent> aggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();
    CreateProductUseCaseImpl createProductUseCase = new CreateProductUseCaseImpl(createProductRepository, aggregateEventDispatcher);

    @Nested
    @DisplayName("Quand il existe déjà un produit avec ce nom")
    public class WhenProductWithGivenNameExists {

        @BeforeEach
        public void setup() {
            createProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withName("product 1")
                            .build()
            );
        }
        @Test
        @DisplayName("Le résultat de la création doit etre un échec")
        public void shouldReturnFailureResult() {
            var result = createProductUseCase.execute(new CreateProductCommand("product 1",
                    BigDecimal.valueOf(50), List.of("blue")));
            ResultTestSupport.assertFailure(result, FailureType.BUSINESS_RULE, ProductErrorCode.PRODUCT_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("Aucun produit n'est persisté, aucun évenement n'est emis")
        public void shouldNotPersistAndDispatchEvent() {
            ResultTestSupport.assertFailure(createProductUseCase.execute(new CreateProductCommand("product 1",
                    BigDecimal.valueOf(50), List.of("blue"))));
            Assertions.assertThat(createProductRepository.count())
                    .isEqualTo(1);
            Assertions.assertThat(aggregateEventDispatcher.count())
                    .isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Quand le produit est OK")
    public class WhenProductCanBeCreated {

        @Test
        @DisplayName("Le produit se crée, se persiste et un évenement est émis")
        public void shouldCreateAndPersistProductAndDispatchEvent() {

            var command = new CreateProductCommand("bonnet", BigDecimal.valueOf(50), List.of("black"));
            var result = createProductUseCase.execute(command);
            ResultTestSupport.assertSuccess(result);

            Assertions.assertThat(createProductRepository.count())
                    .isOne();
            Assertions.assertThat(aggregateEventDispatcher.count())
                    .isOne();

            var persistedProduct = createProductRepository.getLast();

            Assertions.assertThat(persistedProduct.name())
                    .isEqualTo("bonnet");
            Assertions.assertThat(persistedProduct.price())
                    .isEqualByComparingTo(BigDecimal.valueOf(50));
            Assertions.assertThat(persistedProduct.colors())
                    .containsOnly("black");
            Assertions.assertThat(persistedProduct.productStatus())
                    .isEqualByComparingTo(ProductStatus.UNAVAILABLE);
        }
    }
}