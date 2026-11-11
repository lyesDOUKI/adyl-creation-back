package ld.domain.features.order.cancel;


import ld.domain.features.order.lifecycle.InMemoryOrderEditor;
import ld.domain.features.order.model.*;
import ld.domain.features.shared.OrderSnapshotTestBuilder;
import ld.domain.valueObjects.Percentage;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.standard.lib.helper.test.InMemoryUnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static ld.standard.lib.helper.test.ResultTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class CancelOrderUseCaseTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-30T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private final InMemoryOrderEditor inMemoryOrderLifecycleRepository = new InMemoryOrderEditor();
    private final InMemoryAggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();
    private final InMemoryUnitOfWork unitOfWork = new InMemoryUnitOfWork();
    private final CancelOrderUseCase cancelOrderUseCase
            = new CancelOrderUseCaseImpl(inMemoryOrderLifecycleRepository, orderEventAggregateEventDispatcher, unitOfWork, FIXED_CLOCK);

    @Nested
    @DisplayName("Quand la commande n'est pas trouvé")
    class WhenOrderNotFound {

        UUID unknownOrder = UUID.randomUUID();
        @Test
        @DisplayName("L'annulation echoue avec un erreur de commande introuvable")
        void shouldFailedToCancelOrder() {
            var result = cancelOrderUseCase.execute(new CancelOrderCommand(unknownOrder));
            assertFailureOfType(result, CancelOrderError.OrderNotFound.class);
        }

        @Test
        @DisplayName("Rien n'est persisté et aucun événement n'est émis")
        void shouldNotPersistNorDispatchEvent() {
            cancelOrderUseCase.execute(new CancelOrderCommand(unknownOrder));
            assertThat(inMemoryOrderLifecycleRepository.findById(unknownOrder))
                    .isEmpty();
            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est trouvé")
    class WhenOrderFound {

        UUID existingOrderId = UUID.randomUUID();

        private void addOrder(OrderStatus orderStatus) {
            inMemoryOrderLifecycleRepository
                    .save(OrderSnapshotTestBuilder
                            .anOrder(FIXED_CLOCK)
                            .withOrderId(existingOrderId)
                            .withOrderStatus(orderStatus)
                            .build());
            inMemoryOrderLifecycleRepository.reset();
        }

        @Nested
        @DisplayName("avec un statut ACCEPTED")
        class WithAcceptedStatus {

            @BeforeEach
            void setup() {
                addOrder(new OrderStatus.Accepted(FIXED_INSTANT, Percentage.ZERO));
            }
            @Test
            @DisplayName("L'annulation echoue avec un erreur de commande déjà acceptée")
            void shouldFailedToCancelOrder() {
                var result = cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                assertFailureOfType(result, CancelOrderError.OrderAlreadyAccepted.class);
            }

            @Test
            @DisplayName("Rien n'est persisté et aucun événement n'est émis")
            void shouldNotPersistNorDispatchEvent() {
                cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                var existingOrder = inMemoryOrderLifecycleRepository
                        .findById(existingOrderId)
                        .orElseThrow();
                assertThat(inMemoryOrderLifecycleRepository.attempt()).isZero();
                assertThat(existingOrder.orderStatus()).isInstanceOf(OrderStatus.Accepted.class);
                assertThat(orderEventAggregateEventDispatcher.count())
                        .isZero();
            }
        }

        @Nested
        @DisplayName("avec un statut DELIVERED")
        class WithDeliveredStatus {

            @BeforeEach
            void setup() {
                addOrder(new OrderStatus.Delivered("Thanks", DeliveryMethod.HAND_DELIVERY, FIXED_INSTANT));
            }
            @Test
            @DisplayName("L'annulation echoue avec un erreur de commande déjà livrée")
            void shouldFailedToCancelOrder() {
                var result = cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                assertFailureOfType(result, CancelOrderError.OrderAlreadyDelivered.class);
            }

            @Test
            @DisplayName("Rien n'est persisté et aucun événement n'est émis")
            void shouldNotPersistNorDispatchEvent() {
                cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                var existingOrder = inMemoryOrderLifecycleRepository
                        .findById(existingOrderId)
                        .orElseThrow();
                assertThat(inMemoryOrderLifecycleRepository.attempt()).isZero();
                assertThat(existingOrder.orderStatus()).isInstanceOf(OrderStatus.Delivered.class);
                assertThat(orderEventAggregateEventDispatcher.count())
                        .isZero();
            }
        }

        @Nested
        @DisplayName("avec un statut REJECTED")
        class WithRejectedStatus {

            @BeforeEach
            void setup() {
                addOrder(new OrderStatus.Rejected("time", FIXED_INSTANT));
            }
            @Test
            @DisplayName("L'annulation echoue avec un erreur de commande déjà rejetée")
            void shouldFailedToCancelOrder() {
                var result = cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                assertFailureOfType(result, CancelOrderError.OrderAlreadyRejected.class);
            }

            @Test
            @DisplayName("Rien n'est persisté et aucun événement n'est émis")
            void shouldNotPersistNorDispatchEvent() {
                cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                var existingOrder = inMemoryOrderLifecycleRepository
                        .findById(existingOrderId)
                        .orElseThrow();
                assertThat(inMemoryOrderLifecycleRepository.attempt()).isZero();
                assertThat(existingOrder.orderStatus()).isInstanceOf(OrderStatus.Rejected.class);
                assertThat(orderEventAggregateEventDispatcher.count())
                        .isZero();
            }
        }
        @Nested
        @DisplayName("avec un statut PENDING")
        class WithPendingStatus {

            @BeforeEach
            void setup() {
                addOrder(OrderStatus.PENDING);
            }
            @Test
            @DisplayName("l'annulation a réussi, le statut de la commande passe à 'CANCELLED'")
            void shouldSucceedToCancelOrder() {
                var result = cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                assertSuccess(result);
                var persistedOrder = extractValue(result);
                assertThat(persistedOrder.orderStatus()).isInstanceOf(OrderStatus.Cancelled.class);
            }

            @Test
            @DisplayName("La commande est persisté est un événement est emis")
            void shouldPersistAndDispatchEvent() {
                cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                assertThat(inMemoryOrderLifecycleRepository.attempt()).isOne();
                var persisted = inMemoryOrderLifecycleRepository.findById(existingOrderId).orElseThrow();
                assertThat(persisted.orderStatus()).isInstanceOf(OrderStatus.Cancelled.class);
                var status = (OrderStatus.Cancelled) persisted.orderStatus();
                assertThat(status.type()).isEqualTo(OrderState.CANCELLED);
                assertThat(status.cancelledAt()).isEqualTo(FIXED_INSTANT);
                assertThat(orderEventAggregateEventDispatcher.count())
                        .isOne();
                var event = orderEventAggregateEventDispatcher.getDispatchedEvents().getFirst();
                assertThat(event).isInstanceOf(OrderCancelled.class);
                assertThat(((OrderCancelled) event).cancelledAt()).isEqualTo(FIXED_INSTANT);
                assertThat(((OrderCancelled) event).orderId()).isEqualTo(existingOrderId);
            }
        }

        @Nested
        @DisplayName("avec un statut CANCELLED")
        class WithCancelledStatus {

            @BeforeEach
            void setup() {
                addOrder(new OrderStatus.Cancelled(FIXED_INSTANT));
            }
            @Test
            @DisplayName("l'annulation a réussi, le statut de la commande reste à 'CANCELLED'")
            void shouldSucceedToCancelOrder() {
                var result = cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                assertSuccess(result);
                var persistedOrder = extractValue(result);
                assertThat(persistedOrder.orderStatus()).isInstanceOf(OrderStatus.Cancelled.class);
            }

            @Test
            @DisplayName("la commande est persisté et AUCUN événement n'est émis")
            void shouldPersistButNotDispatchEvent() {
                cancelOrderUseCase.execute(new CancelOrderCommand(existingOrderId));
                assertThat(inMemoryOrderLifecycleRepository.attempt()).isOne();
                var persisted = inMemoryOrderLifecycleRepository.findById(existingOrderId).orElseThrow();
                assertThat(persisted.orderStatus()).isInstanceOf(OrderStatus.Cancelled.class);
                var status = (OrderStatus.Cancelled) persisted.orderStatus();
                assertThat(status.type()).isEqualTo(OrderState.CANCELLED);
                assertThat(status.cancelledAt()).isEqualTo(FIXED_INSTANT);
                assertThat(orderEventAggregateEventDispatcher.count())
                        .isZero();
            }
        }
    }
}