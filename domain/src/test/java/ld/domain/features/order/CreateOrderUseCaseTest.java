package ld.domain.features.order;


import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.order.model.OrderStatus;
import ld.domain.helper.ResultTestSupport;
import ld.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.lib.validation.FailureType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CreateOrderUseCaseTest {

    private final CreateOrderUseCase createOrderUseCase;
    private final InMemoryCreateOrderRepository createOrderRepository = new InMemoryCreateOrderRepository();
    private final InMemoryAggregateEventDispatcher<OrderEvent> aggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();

    CreateOrderUseCaseTest() {
        this.createOrderUseCase = new CreateOrderUseCaseImpl(createOrderRepository,
                aggregateEventDispatcher);
    }

    private CreateOrderCommand defaultCommand(UUID productId, BigDecimal price) {
        return new CreateOrderCommand("test",
                "0123456789",
                "test@test.com",
                "7 rue test",
                "avignon",
                Optional.empty(),
                List.of(new CreateOrderCommand.CreateOrderItem(
                        productId,
                        price,
                        1,
                        "blue"
                ))
        );
    }

    private CreateOrderCommand withItems(List<CreateOrderCommand.CreateOrderItem> items) {
        return new CreateOrderCommand("test",
                "0123456789",
                "test@test.com",
                "7 rue test",
                "avignon",
                Optional.empty(),
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
                    productId,
                    BigDecimal.valueOf(50)
            );

            createOrderRepository.addProduct(UUID.randomUUID());

            var result = createOrderUseCase.execute(command);
            ResultTestSupport.assertFailure(result, FailureType.RESOURCE_NOT_FOUND);
        }

        @Test
        @DisplayName("Rien n'est persisté et aucun événement n'est publié")
        void shouldNotPersistCommand() {
            var productId = UUID.randomUUID();
            var command = defaultCommand(
                    productId,
                    BigDecimal.valueOf(50)
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

            createOrderRepository.addProduct(productId);

            var command = defaultCommand(
                    productId,
                    BigDecimal.valueOf(50)
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

            createOrderRepository.addProduct(productOne);
            createOrderRepository.addProduct(productTwo);
            createOrderRepository.addProduct(productThree);

            var createOrderItems = List.of(
                    new CreateOrderCommand.CreateOrderItem(
                            productOne,
                            BigDecimal.valueOf(100),
                            2,
                            "blue"
                    ),
                    new CreateOrderCommand.CreateOrderItem(
                            productTwo,
                            BigDecimal.valueOf(100),
                            1,
                            "noir"
                    ),
                    new CreateOrderCommand.CreateOrderItem(
                            productThree,
                            BigDecimal.valueOf(200),
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
}