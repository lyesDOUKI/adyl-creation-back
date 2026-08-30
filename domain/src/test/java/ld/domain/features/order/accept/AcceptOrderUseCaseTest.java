package ld.domain.features.order.accept;


import ld.domain.features.order.lifecycle.InMemoryOrderLifecycleRepository;
import ld.domain.features.order.lifecycle.OrderLifecycleRepository;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderStatus;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.features.shared.OrderSnapshotTestBuilder;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.standard.lib.validation.FailureType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static ld.standard.lib.helper.test.ResultTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class AcceptOrderUseCaseTest {

    private final OrderLifecycleRepository orderLifecycleRepository = new InMemoryOrderLifecycleRepository();
    private final InMemoryAggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();
    private final AcceptOrderUseCase acceptOrderUseCase =
            new AcceptOrderUseCaseImpl(orderLifecycleRepository, orderEventAggregateEventDispatcher);

    @Nested
    @DisplayName("Quand la commande n'est pas trouvée")
    class WhenOrderNotFound {

        @Test
        @DisplayName("L'acceptation échoue avec une erreur resource introuvable")
        void shouldFailToAcceptOrder() {
            var command = new AcceptOrderCommand(UUID.randomUUID());

            var result = acceptOrderUseCase.execute(command);

            assertFailure(result, FailureType.RESOURCE_NOT_FOUND, OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("Rien n'est persisté et aucun événement n'est publié")
        void shouldNotPersistNorDispatchEvent() {
            var command = new AcceptOrderCommand(UUID.randomUUID());

            assertFailure(acceptOrderUseCase.execute(command));

            assertThat(orderLifecycleRepository.findById(command.orderId()))
                    .isEmpty();

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est en statut PENDING")
    class WhenOrderIsPending {

        @Test
        @DisplayName("L'acceptation réussit et la commande passe au statut ACCEPTED")
        void shouldChangeOrderStatusToAccepted() {
            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withTotal(BigDecimal.valueOf(50))
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);

            var acceptedOrder = extractValue(result);
            assertThat(acceptedOrder.orderStatus())
                    .isEqualByComparingTo(OrderStatus.ACCEPTED);
        }

        @Test
        @DisplayName("La commande est persistée avec le nouveau statut et un événement est émis")
        void shouldPersistOrderAndDispatchOrderAcceptedEvent() {
            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withTotal(BigDecimal.valueOf(50))
                            .build()
            );

            assertSuccess(acceptOrderUseCase.execute(new AcceptOrderCommand(orderId)));

            var persistedOrder = orderLifecycleRepository.findById(orderId);
            assertThat(persistedOrder)
                    .isPresent();
            assertThat(persistedOrder.get().orderStatus())
                    .isEqualByComparingTo(OrderStatus.ACCEPTED);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isOne();
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut DELIVERED")
    class WhenOrderIsAlreadyDelivered {

        @Test
        @DisplayName("L'acceptation échoue avec une erreur business")
        void shouldFailToAcceptOrder() {
            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.DELIVERED)
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertFailure(result, FailureType.BUSINESS_RULE, OrderErrorCode.ORDER_HAS_BEEN_DELIVERED);
        }

        @Test
        @DisplayName("Le statut n'est pas modifié et aucun événement n'est publié")
        void shouldNotChangeStatusNorDispatchEvent() {
            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.DELIVERED)
                            .build()
            );

            assertFailure(acceptOrderUseCase.execute(new AcceptOrderCommand(orderId)));

            var persistedOrder = orderLifecycleRepository.findById(orderId);
            assertThat(persistedOrder)
                    .isPresent();
            assertThat(persistedOrder.get().orderStatus())
                    .isEqualByComparingTo(OrderStatus.DELIVERED);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut REJECTED")
    class WhenOrderIsAlreadyRejected {

        @Test
        @DisplayName("L'acceptation échoue avec une erreur business")
        void shouldFailToAcceptOrder() {
            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.REJECTED)
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertFailure(result, FailureType.BUSINESS_RULE, OrderErrorCode.ORDER_HAS_BEEN_REJECTED);
        }

        @Test
        @DisplayName("Le statut n'est pas modifié et aucun événement n'est publié")
        void shouldNotChangeStatusNorDispatchEvent() {
            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.REJECTED)
                            .build()
            );

            assertFailure(acceptOrderUseCase.execute(new AcceptOrderCommand(orderId)));

            var persistedOrder = orderLifecycleRepository.findById(orderId);
            assertThat(persistedOrder)
                    .isPresent();
            assertThat(persistedOrder.get().orderStatus())
                    .isEqualByComparingTo(OrderStatus.REJECTED);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }
}