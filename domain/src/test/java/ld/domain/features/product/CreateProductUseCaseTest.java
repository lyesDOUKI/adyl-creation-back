package ld.domain.features.product;

import ld.domain.features.product.model.ProductCategory;
import ld.domain.features.product.model.ProductColor;
import ld.domain.features.product.model.ProductEvent;
import ld.domain.features.product.model.ProductStatus;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.domain.features.shared.ProductSnapshotTestBuilder;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.standard.lib.helper.test.InMemoryUnitOfWork;
import ld.standard.lib.validation.FailureType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ld.standard.lib.helper.test.ResultTestSupport.*;

class CreateProductUseCaseTest {

    InMemoryProductRepository repository = new InMemoryProductRepository();
    InMemoryAggregateEventDispatcher<ProductEvent> aggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();
    InMemoryUnitOfWork unitOfWork = new InMemoryUnitOfWork();
    CreateProductUseCaseImpl createProductUseCase = new CreateProductUseCaseImpl(repository,
            repository,
            aggregateEventDispatcher, unitOfWork);

    private static CreateProductCommand createCommand() {
        return CreateProductCommandTestBuilder.aCreateProductCommand()
                .build();
    }

    @Nested
    @DisplayName("Quand il existe déjà un produit avec ce nom")
    public class WhenProductWithGivenNameExists {

        @BeforeEach
        public void setup() {
            repository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withName("product 1")
                            .build()
            );
        }
        @Test
        @DisplayName("Le résultat de la création doit etre un échec")
        public void shouldReturnFailureResult() {
            var result = createProductUseCase.execute(createCommand());
            assertFailure(result, FailureType.BUSINESS_RULE, ProductErrorCode.PRODUCT_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("Aucun produit n'est persisté, aucun évenement n'est emis")
        public void shouldNotPersistAndDispatchEvent() {
            assertFailure(createProductUseCase.execute(createCommand()));
            Assertions.assertThat(repository.count())
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
            var command = CreateProductCommandTestBuilder.aCreateProductCommand()
                    .withName("bonnet")
                    .withColors("black")
                    .build();

            var result = createProductUseCase.execute(command);
            assertSuccess(result);

            Assertions.assertThat(repository.count())
                    .isOne();
            Assertions.assertThat(aggregateEventDispatcher.count())
                    .isOne();

            var persistedProduct = extractValue(result);

            Assertions.assertThat(persistedProduct.name())
                    .isEqualTo("bonnet");
            Assertions.assertThat(persistedProduct.price())
                    .isEqualByComparingTo(BigDecimal.valueOf(50));
            Assertions.assertThat(persistedProduct.colors())
                    .containsOnly(new ProductColor("black"));
            Assertions.assertThat(persistedProduct.productStatus())
                    .isEqualByComparingTo(ProductStatus.AVAILABLE);
        }

        @Test
        public void withoutColors_shouldCreateAndPersistProductAndDispatchEvent() {
            var command = CreateProductCommandTestBuilder.aCreateProductCommand()
                    .withName("bonnet")
                    .withoutColors()
                    .build();

            var result = createProductUseCase.execute(command);
            assertSuccess(result);

            Assertions.assertThat(repository.count())
                    .isOne();
            Assertions.assertThat(aggregateEventDispatcher.count())
                    .isOne();

            var persistedProduct = extractValue(result);

            Assertions.assertThat(persistedProduct.name())
                    .isEqualTo("bonnet");
            Assertions.assertThat(persistedProduct.price())
                    .isEqualByComparingTo(BigDecimal.valueOf(50));
            Assertions.assertThat(persistedProduct.colors())
                    .isEmpty();
            Assertions.assertThat(persistedProduct.productStatus())
                    .isEqualByComparingTo(ProductStatus.AVAILABLE);
        }

        @Test
        public void witNullColors_shouldCreateAndPersistProductAndDispatchEvent() {
            List<String> colors = new ArrayList<>();
            colors.add(null);
            var command = CreateProductCommandTestBuilder.aCreateProductCommand()
                    .withName("bonnet")
                    .withColors((String) null)
                    .build();

            var result = createProductUseCase.execute(command);
            assertSuccess(result);

            Assertions.assertThat(repository.count())
                    .isOne();
            Assertions.assertThat(aggregateEventDispatcher.count())
                    .isOne();

            var persistedProduct = extractValue(result);

            Assertions.assertThat(persistedProduct.name())
                    .isEqualTo("bonnet");
            Assertions.assertThat(persistedProduct.price())
                    .isEqualByComparingTo(BigDecimal.valueOf(50));
            Assertions.assertThat(persistedProduct.colors())
                    .isEmpty();
            Assertions.assertThat(persistedProduct.productStatus())
                    .isEqualByComparingTo(ProductStatus.AVAILABLE);
        }

        @Test
        public void shouldCreateProductWithGivenCategory() {
            var command = CreateProductCommandTestBuilder.aCreateProductCommand()
                    .withName("bonnet")
                    .withCategory(ProductCategory.CLOTHING)
                    .build();

            var result = createProductUseCase.execute(command);
            assertSuccess(result);

            var persistedProduct = extractValue(result);

            Assertions.assertThat(persistedProduct.productCategory())
                    .isEqualTo(ProductCategory.CLOTHING);
        }
    }

    private static final class CreateProductCommandTestBuilder {

        private String name = "product 1";
        private ProductCategory productCategory = ProductCategory.ACCESSORIES;
        private final BigDecimal price = BigDecimal.valueOf(50);
        private List<String> colors = List.of("blue");

        public static CreateProductCommandTestBuilder aCreateProductCommand() {
            return new CreateProductCommandTestBuilder();
        }

        public CreateProductCommandTestBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public CreateProductCommandTestBuilder withCategory(ProductCategory productCategory) {
            this.productCategory = productCategory;
            return this;
        }

        public CreateProductCommandTestBuilder withColors(String... colors) {
            this.colors = Arrays.stream(colors).toList();
            return this;
        }

        public CreateProductCommandTestBuilder withoutColors() {
            this.colors = null;
            return this;
        }

        public CreateProductCommand build() {
            return new CreateProductCommand(
                    name,
                    productCategory,
                    price,
                    colors
            );
        }
    }
}
