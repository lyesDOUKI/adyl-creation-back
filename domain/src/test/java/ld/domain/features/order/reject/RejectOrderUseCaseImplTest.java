package ld.domain.features.order.reject;

import ld.domain.features.order.lifecycle.InMemoryOrderEditor;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderState;
import ld.domain.features.order.model.OrderStatus;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.features.shared.OrderSnapshotTestBuilder;
import ld.domain.valueObjects.Percentage;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.standard.lib.helper.test.InMemoryUnitOfWork;
import ld.standard.lib.validation.FailureType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static ld.standard.lib.helper.test.ResultTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class RejectOrderUseCaseTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-30T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private final InMemoryOrderEditor inMemoryOrderLifecycleRepository = new InMemoryOrderEditor();
    private final InMemoryAggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();
    private final InMemoryUnitOfWork unitOfWork = new InMemoryUnitOfWork();

    private final RejectOrderUseCase rejectOrderUseCase = new RejectOrderUseCaseImpl(
            inMemoryOrderLifecycleRepository,
            orderEventAggregateEventDispatcher,
            unitOfWork,
            FIXED_CLOCK
    );

    @Nested
    @DisplayName("Quand la commande n'est pas trouvée")
    class WhenOrderNotFound {

        @Test
        @DisplayName("Le rejet échoue avec une erreur resource introuvable")
        void shouldFailToRejectOrder() {
            var command = new RejectOrderCommand(
                    UUID.randomUUID(),
                    "Produit indisponible"
            );

            var result = rejectOrderUseCase.execute(command);

            assertFailure(
                    result,
                    FailureType.RESOURCE_NOT_FOUND,
                    OrderErrorCode.ORDER_NOT_FOUND
            );
        }

        @Test
        @DisplayName("Rien n'est persisté et aucun événement n'est publié")
        void shouldNotPersistNorDispatchEvent() {
            var command = new RejectOrderCommand(
                    UUID.randomUUID(),
                    "Produit indisponible"
            );

            rejectOrderUseCase.execute(command);

            assertThat(inMemoryOrderLifecycleRepository.findById(command.orderId()))
                    .isEmpty();
            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est en statut PENDING")
    class WhenOrderIsPending {

        @Test
        @DisplayName("Le rejet réussit, la commande passe au statut REJECTED avec la raison et la date figée")
        void shouldChangeOrderStatusToRejected() {
            var orderId = UUID.randomUUID();
            var reason = "Produit indisponible";

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .build()
            );

            var result = rejectOrderUseCase.execute(
                    new RejectOrderCommand(orderId, reason)
            );

            assertSuccess(result);

            var rejectedOrder = extractValue(result);

            assertThat(rejectedOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Rejected.class);

            var rejectedStatus = (OrderStatus.Rejected) rejectedOrder.orderStatus();

            assertThat(rejectedStatus.reason())
                    .isEqualTo(reason);

            assertThat(rejectedStatus.rejectedAt())
                    .isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("La commande est persistée avec le nouveau statut et un événement est émis")
        void shouldPersistOrderAndDispatchOrderRejectedEvent() {
            var orderId = UUID.randomUUID();
            var reason = "Produit indisponible";

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .build()
            );

            assertSuccess(
                    rejectOrderUseCase.execute(
                            new RejectOrderCommand(orderId, reason)
                    )
            );

            var persistedOrder = inMemoryOrderLifecycleRepository
                    .findById(orderId)
                    .orElseThrow();

            assertThat(persistedOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Rejected.class);

            assertThat(persistedOrder.orderStatus().type())
                    .isEqualTo(OrderState.REJECTED);

            var rejectedStatus = (OrderStatus.Rejected) persistedOrder.orderStatus();

            assertThat(rejectedStatus.reason())
                    .isEqualTo(reason);

            assertThat(rejectedStatus.rejectedAt())
                    .isEqualTo(FIXED_INSTANT);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isOne();
        }
    }

    @Nested
    @DisplayName("Quand la commande est en statut ACCEPTED")
    class WhenOrderIsAccepted {

        @Test
        @DisplayName("Le rejet réussit, la commande passe au statut REJECTED")
        void shouldChangeOrderStatusToRejected() {
            var orderId = UUID.randomUUID();
            var reason = "Client non joignable";

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(
                                    new OrderStatus.Accepted(
                                            FIXED_INSTANT,
                                            Percentage.ZERO
                                    )
                            )
                            .build()
            );

            var result = rejectOrderUseCase.execute(
                    new RejectOrderCommand(orderId, reason)
            );

            assertSuccess(result);

            var rejectedOrder = extractValue(result);

            assertThat(rejectedOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Rejected.class);

            var rejectedStatus = (OrderStatus.Rejected) rejectedOrder.orderStatus();

            assertThat(rejectedStatus.reason())
                    .isEqualTo(reason);

            assertThat(rejectedStatus.rejectedAt())
                    .isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("Un événement OrderRejected est publié")
        void shouldDispatchOrderRejectedEvent() {
            var orderId = UUID.randomUUID();
            var reason = "Client non joignable";

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(
                                    new OrderStatus.Accepted(
                                            FIXED_INSTANT,
                                            Percentage.ZERO
                                    )
                            )
                            .build()
            );

            assertSuccess(
                    rejectOrderUseCase.execute(
                            new RejectOrderCommand(orderId, reason)
                    )
            );

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isOne();
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut DELIVERED")
    class WhenOrderIsAlreadyDelivered {

        @Test
        @DisplayName("Le rejet échoue avec une erreur business")
        void shouldFailToRejectOrder() {
            var orderId = UUID.randomUUID();

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.DELIVERED)
                            .build()
            );

            var result = rejectOrderUseCase.execute(
                    new RejectOrderCommand(
                            orderId,
                            "Produit indisponible"
                    )
            );

            assertFailure(
                    result,
                    FailureType.BUSINESS_RULE,
                    OrderErrorCode.ORDER_HAS_BEEN_DELIVERED
            );
        }

        @Test
        @DisplayName("Le statut n'est pas modifié et aucun événement n'est publié")
        void shouldNotChangeStatusNorDispatchEvent() {
            var orderId = UUID.randomUUID();

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.DELIVERED)
                            .build()
            );

            rejectOrderUseCase.execute(
                    new RejectOrderCommand(
                            orderId,
                            "Produit indisponible"
                    )
            );

            var persistedOrder = inMemoryOrderLifecycleRepository
                    .findById(orderId)
                    .orElseThrow();

            assertThat(persistedOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Delivered.class);

            assertThat(persistedOrder.orderStatus().type())
                    .isEqualTo(OrderState.DELIVERED);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut REJECTED")
    class WhenOrderIsAlreadyRejected {

        @Test
        @DisplayName("Le rejet réussit sans modifier la commande")
        void shouldReturnSuccessWithoutChangingAnything() {
            var orderId = UUID.randomUUID();
            var existingReason = "Produit indisponible";

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(
                                    new OrderStatus.Rejected(
                                            existingReason,
                                            FIXED_INSTANT
                                    )
                            )
                            .build()
            );

            var result = rejectOrderUseCase.execute(
                    new RejectOrderCommand(
                            orderId,
                            "Nouvelle raison"
                    )
            );

            assertSuccess(result);

            var rejectedOrder = extractValue(result);

            assertThat(rejectedOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Rejected.class);

            var rejectedStatus = (OrderStatus.Rejected) rejectedOrder.orderStatus();

            assertThat(rejectedStatus.reason())
                    .isEqualTo(existingReason);

            assertThat(rejectedStatus.rejectedAt())
                    .isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("Aucun événement supplémentaire n'est publié")
        void shouldNotDispatchDuplicateEvent() {
            var orderId = UUID.randomUUID();

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(
                                    new OrderStatus.Rejected(
                                            "Produit indisponible",
                                            FIXED_INSTANT
                                    )
                            )
                            .build()
            );

            assertSuccess(
                    rejectOrderUseCase.execute(
                            new RejectOrderCommand(
                                    orderId,
                                    "Nouvelle raison"
                            )
                    )
            );

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }
}