package ld.domain.features.order;


import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.features.product.InMemoryGetProductRepository;
import ld.domain.features.product.model.ProductColor;
import ld.domain.features.product.model.ProductStatus;
import ld.domain.features.shared.ProductSnapshotTestBuilder;
import ld.domain.helper.ResultTestSupport;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.standard.lib.validation.FailureType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CreateOrderUseCaseTest {

    private final CreateOrderUseCase createOrderUseCase;
    private final InMemoryCreateOrderRepository createOrderRepository = new InMemoryCreateOrderRepository();
    private final InMemoryGetProductRepository getProductRepository = new InMemoryGetProductRepository();
    private final InMemoryAggregateEventDispatcher<OrderEvent> aggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();

    CreateOrderUseCaseTest() {
        this.createOrderUseCase = new CreateOrderUseCaseImpl(
                createOrderRepository,
                getProductRepository,
                aggregateEventDispatcher
        );
    }

    private CreateOrderCommand.CustomerInfo defaultCustomer() {
        return new CreateOrderCommand.CustomerInfo(
                "test",
                "0123456789",
                "test@test.com",
                "7 rue test",
                "avignon");
    }

    private CreateOrderCommand defaultCommand(UUID productId) {
        return new CreateOrderCommand(
                defaultCustomer(),
                "no message",
                List.of(new CreateOrderCommand.CreateOrderItem(
                        productId,
                        1,
                        "blue"
                ))
        );
    }

    private CreateOrderCommand withItems(List<CreateOrderCommand.CreateOrderItem> items) {
        return new CreateOrderCommand(
                defaultCustomer(),
                "personnal message",
                items
        );
    }

    @Nested
    @DisplayName("Quand le produit demandé n'est pas trouvé")
    class WhenProductNotFound {

        @Test
        @DisplayName("la création échoue")
        void shouldFailToCreateCommand() {
            var productId = UUID.randomUUID();
            var command = defaultCommand(
                    productId
            );

            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(UUID.randomUUID())
                            .withPrice(BigDecimal.valueOf(50))
                            .build()
            );

            var result = createOrderUseCase.execute(command);
            ResultTestSupport.assertFailure(result, FailureType.RESOURCE_NOT_FOUND, OrderErrorCode.PRODUCTS_NOT_FOUND);
        }

        @Test
        @DisplayName("Rien n'est persisté et aucun événement n'est publié")
        void shouldNotPersistCommand() {
            var productId = UUID.randomUUID();
            var command = defaultCommand(
                    productId
            );

            ResultTestSupport.assertFailure(createOrderUseCase.execute(command));

            assertThat(createOrderRepository.countOrders())
                    .isZero();

            assertThat(aggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est valide")
    class WhenCommandIsValid {

        @Test
        @DisplayName("La commande est créée avec le bon calcul sur un item")
        void shouldCreateOrderWithCorrectCalculationOnOneItem() {
            var productId = UUID.randomUUID();

            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(productId)
                            .withPrice(BigDecimal.valueOf(50))
                            .withStatus(ProductStatus.AVAILABLE)
                            .withColors(List.of(
                                    new ProductColor("blue")
                                    )
                            )
                            .build()
            );

            var command = defaultCommand(
                    productId
            );

            var result = createOrderUseCase.execute(command);
            ResultTestSupport.assertSuccess(result);

            assertThat(createOrderRepository.countOrders())
                    .isOne();

            assertThat(aggregateEventDispatcher.count())
                    .isOne();


            var persistedOrder = createOrderRepository.findCreatedOrder();

            assertThat(persistedOrder.orderStatus())
                    .isEqualByComparingTo(OrderStatus.PENDING);
            assertThat(persistedOrder.total())
                    .isEqualByComparingTo(BigDecimal.valueOf(50));

            assertThat(persistedOrder.items())
                    .hasSize(1);

            var item = persistedOrder.items().getFirst();

            assertThat(item.total())
                    .isEqualByComparingTo(BigDecimal.valueOf(50));
        }

        @Test
        @DisplayName("La commande est créée avec le bon calcul sur plusieurs items")
        void shouldCreateOrderWithCorrectCalculationOnMultipleItems() {
            var productOne = UUID.randomUUID();
            var productTwo = UUID.randomUUID();
            var productThree = UUID.randomUUID();

            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(productOne)
                            .withPrice(BigDecimal.valueOf(100))
                            .withStatus(ProductStatus.AVAILABLE)
                            .withColors(List.of(
                            new ProductColor("blue")
                                    )
                            )
                            .build()
            );
            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(productTwo)
                            .withPrice(BigDecimal.valueOf(100))
                            .withStatus(ProductStatus.AVAILABLE)
                            .withColors(List.of(
                                    new ProductColor("blue"),
                                    new ProductColor("noir")
                                    )
                            )
                            .build()
            );
            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(productThree)
                            .withPrice(BigDecimal.valueOf(200))
                            .withStatus(ProductStatus.AVAILABLE)
                            .withColors(List.of(
                                            new ProductColor("vert"),
                                            new ProductColor("rouge")
                                    )
                            )
                            .build()
            );

            var createOrderItems = List.of(
                    new CreateOrderCommand.CreateOrderItem(
                            productOne,
                            2,
                            "blue"
                    ),
                    new CreateOrderCommand.CreateOrderItem(
                            productTwo,
                            1,
                            "noir"
                    ),
                    new CreateOrderCommand.CreateOrderItem(
                            productThree,
                            4,
                            "rouge"
                    )
            );

            var command = withItems(createOrderItems);

            var result = createOrderUseCase.execute(command);
            ResultTestSupport.assertSuccess(result);

            var persistedOrder = createOrderRepository.findCreatedOrder();
            assertThat(persistedOrder.orderStatus())
                    .isEqualByComparingTo(OrderStatus.PENDING);
            assertThat(persistedOrder.total())
                    .isEqualByComparingTo(BigDecimal.valueOf(1100));

            assertThat(persistedOrder.items())
                    .hasSize(3);

            assertThat(persistedOrder.items())
                    .extracting(OrderSnapshot.OrderItemSnapshot::productId)
                    .containsExactlyInAnyOrder(
                            productOne,
                            productTwo,
                            productThree
                    );

            assertThat(persistedOrder.items())
                    .extracting(
                            OrderSnapshot.OrderItemSnapshot::productId,
                            OrderSnapshot.OrderItemSnapshot::total
                    )
                    .containsExactlyInAnyOrder(
                            tuple(productOne, BigDecimal.valueOf(200)),
                            tuple(productTwo, BigDecimal.valueOf(100)),
                            tuple(productThree, BigDecimal.valueOf(800))
                    );
        }
    }

    @Nested
    @DisplayName("Quand un des produits demandé est en statut indisponible")
    public class WhenOneOfGivenProductsIsUnavailable {

        @Test
        @DisplayName("Une erreur business doit etre remonté, rien ne doit etre persisté et aucun evement n'est emis")
        public void shouldReturnFailureResultAndNotPersistAndNotDispatchEvent() {
            var productOne = UUID.randomUUID();
            var productTwo = UUID.randomUUID();

            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                    .withId(productOne).withPrice(BigDecimal.valueOf(100))
                    .withName("unavailable product")
                            .build()
            );
            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(productTwo)
                            .withPrice(BigDecimal.valueOf(100))
                            .withStatus(ProductStatus.AVAILABLE)
                            .build()
            );

            var createOrderItems = List.of(
                    new CreateOrderCommand.CreateOrderItem(
                            productOne,
                            2,
                            "blue"
                    ),
                    new CreateOrderCommand.CreateOrderItem(
                            productTwo,
                            1,
                            "noir"
                    ));
            var command = withItems(createOrderItems);

            var result = createOrderUseCase.execute(command);

            ResultTestSupport.assertFailure(result, FailureType.BUSINESS_RULE, OrderErrorCode.PRODUCT_NOT_AVAILABLE);

            assertThat(createOrderRepository.countOrders())
                    .isZero();

            assertThat(aggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la couleur d'un des produits disponible demandé n'est pas présente dans les couleurs possible du produit")
    public class WhenOneOfGivenColorIsNotInTheListOfGivenChosenProduct {

        @Test
        @DisplayName("Une erreur business doit etre remonté, rien ne doit etre persisté et aucun evement n'est emis")
        public void shouldReturnFailureResultAndNotPersistAndNotDispatchEvent() {

            UUID productId = UUID.randomUUID();
            var command = withItems(List.of(
                    new CreateOrderCommand.CreateOrderItem(
                            productId,
                            2,
                            "red"
                    )
            ));
            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(productId)
                            .withColors(List.of(
                                            new ProductColor("black")
                                    )
                            )
                            .withStatus(ProductStatus.AVAILABLE)
                            .build()
            );

            ResultTestSupport.assertFailure(createOrderUseCase.execute(command), FailureType.BUSINESS_RULE, OrderErrorCode.PRODUCT_COLOR_NOT_AVAILABLE);
        }
    }

    @Nested
    @DisplayName("Quand un produit n'a pas de couleur défini")
    public class WhenProductDoesntHaveColors {

        @Test
        @DisplayName("La création de la commande est validé, la commande est persisté et un évenement est dispatché")
        public void shouldCreateCommandWithoutBusinessFailure() {
            UUID productOne = UUID.randomUUID();
            UUID productTwo = UUID.randomUUID();

            var command = withItems(List.of(
                    new CreateOrderCommand.CreateOrderItem(productOne,
                            2,
                            "blue"),
                    new CreateOrderCommand.CreateOrderItem(productTwo,
                            1,
                            "rouge")
            ));

            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(productOne)
                            .withColors(null)
                            .withStatus(ProductStatus.AVAILABLE)
                            .build()
            );
            getProductRepository.addProduct(
                    ProductSnapshotTestBuilder.aProduct()
                            .withId(productTwo)
                            .withColors(Collections.emptyList())
                            .withStatus(ProductStatus.AVAILABLE)
                            .build()
            );

            ResultTestSupport.assertSuccess(createOrderUseCase.execute(command));
            assertThat(createOrderRepository.countOrders())
                    .isOne();
            assertThat(aggregateEventDispatcher.count())
                    .isOne();
        }
    }
}