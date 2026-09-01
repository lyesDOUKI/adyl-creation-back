package ld.domain.features.order.deliver;

import ld.domain.features.order.lifecycle.InMemoryOrderEditor;
import ld.domain.features.order.model.DeliveryMethod;
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

class DeliverOrderUseCaseTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-30T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private final InMemoryOrderEditor inMemoryOrderLifecycleRepository = new InMemoryOrderEditor();
    private final InMemoryAggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();
    private final InMemoryUnitOfWork unitOfWork = new InMemoryUnitOfWork();

    private final DeliverOrderUseCase deliverOrderUseCase = new DeliverOrderUseCaseImpl(
            inMemoryOrderLifecycleRepository,
            orderEventAggregateEventDispatcher,
            unitOfWork,
            FIXED_CLOCK
    );

    @Nested
    @DisplayName("Quand la commande n'est pas trouvée")
    class WhenOrderNotFound {

        @Test
        @DisplayName("La livraison échoue avec une erreur resource introuvable")
        void shouldFailToDeliverOrder() {
            var command = new DeliverOrderCommand(
                    UUID.randomUUID(),
                    DeliveryMethod.HAND_DELIVERY,
                    "Remis en main propre"
            );

            var result = deliverOrderUseCase.execute(command);

            assertFailure(
                    result,
                    FailureType.RESOURCE_NOT_FOUND,
                    OrderErrorCode.ORDER_NOT_FOUND
            );
        }

        @Test
        @DisplayName("Rien n'est persisté et aucun événement n'est publié")
        void shouldNotPersistNorDispatchEvent() {
            var command = new DeliverOrderCommand(
                    UUID.randomUUID(),
                    DeliveryMethod.HAND_DELIVERY,
                    "Remis en main propre"
            );

            deliverOrderUseCase.execute(command);

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
        @DisplayName("La livraison échoue avec une erreur business")
        void shouldFailToDeliverOrder() {
            var orderId = UUID.randomUUID();

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .build()
            );

            var result = deliverOrderUseCase.execute(
                    new DeliverOrderCommand(orderId, DeliveryMethod.HAND_DELIVERY, "Remis en main propre")
            );

            assertFailure(
                    result,
                    FailureType.BUSINESS_RULE,
                    OrderErrorCode.PENDING_ORDER
            );
        }

        @Test
        @DisplayName("Le statut n'est pas modifié et aucun événement n'est publié")
        void shouldNotChangeStatusNorDispatchEvent() {
            var orderId = UUID.randomUUID();

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .build()
            );

            deliverOrderUseCase.execute(
                    new DeliverOrderCommand(orderId, DeliveryMethod.HAND_DELIVERY, "Remis en main propre")
            );

            var persistedOrder = inMemoryOrderLifecycleRepository
                    .findById(orderId)
                    .orElseThrow();

            assertThat(persistedOrder.orderStatus())
                    .isEqualTo(OrderStatus.PENDING);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est en statut ACCEPTED")
    class WhenOrderIsAccepted {

        @Test
        @DisplayName("La livraison réussit, la commande passe au statut DELIVERED avec les informations et la date figée")
        void shouldChangeOrderStatusToDelivered() {
            var orderId = UUID.randomUUID();
            var observation = "Remis en main propre";
            var deliveryMethod = DeliveryMethod.HAND_DELIVERY;

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

            var result = deliverOrderUseCase.execute(
                    new DeliverOrderCommand(orderId, deliveryMethod, observation)
            );

            assertSuccess(result);

            var deliveredOrder = extractValue(result);

            assertThat(deliveredOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Delivered.class);

            var deliveredStatus = (OrderStatus.Delivered) deliveredOrder.orderStatus();

            assertThat(deliveredStatus.observation())
                    .isEqualTo(observation);

            assertThat(deliveredStatus.deliveryMethod())
                    .isEqualTo(deliveryMethod);

            assertThat(deliveredStatus.deliveredAt())
                    .isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("La commande est persistée avec le nouveau statut et un événement OrderDelivered est publié")
        void shouldPersistOrderAndDispatchOrderDeliveredEvent() {
            var orderId = UUID.randomUUID();
            var observation = "Remis en main propre";
            var deliveryMethod = DeliveryMethod.HAND_DELIVERY;

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
                    deliverOrderUseCase.execute(
                            new DeliverOrderCommand(orderId, deliveryMethod, observation)
                    )
            );

            var persistedOrder = inMemoryOrderLifecycleRepository
                    .findById(orderId)
                    .orElseThrow();

            assertThat(persistedOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Delivered.class);

            assertThat(persistedOrder.orderStatus().type())
                    .isEqualTo(OrderState.DELIVERED);

            var deliveredStatus = (OrderStatus.Delivered) persistedOrder.orderStatus();

            assertThat(deliveredStatus.observation())
                    .isEqualTo(observation);

            assertThat(deliveredStatus.deliveryMethod())
                    .isEqualTo(deliveryMethod);

            assertThat(deliveredStatus.deliveredAt())
                    .isEqualTo(FIXED_INSTANT);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isOne();
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut DELIVERED")
    class WhenOrderIsAlreadyDelivered {

        @Test
        @DisplayName("La livraison réussit sans modifier la commande")
        void shouldReturnSuccessWithoutChangingAnything() {
            var orderId = UUID.randomUUID();
            var existingObservation = "bonne commande";
            var existingDeliveryMethod = DeliveryMethod.HAND_DELIVERY;

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(new OrderStatus.Delivered(
                                    existingObservation,
                                    existingDeliveryMethod,
                                    FIXED_INSTANT
                            ))
                            .build()
            );

            var result = deliverOrderUseCase.execute(
                    new DeliverOrderCommand(
                            orderId,
                            DeliveryMethod.HAND_DELIVERY,
                            "Nouvelle observation"
                    )
            );

            assertSuccess(result);

            var deliveredOrder = extractValue(result);

            assertThat(deliveredOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Delivered.class);

            var deliveredStatus = (OrderStatus.Delivered) deliveredOrder.orderStatus();

            assertThat(deliveredStatus.observation())
                    .isEqualTo(existingObservation);

            assertThat(deliveredStatus.deliveryMethod())
                    .isEqualTo(existingDeliveryMethod);

            assertThat(deliveredStatus.deliveredAt())
                    .isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("Aucun événement supplémentaire n'est publié")
        void shouldNotDispatchDuplicateEvent() {
            var orderId = UUID.randomUUID();

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(new OrderStatus.Delivered(
                                    "bonne commande",
                                    DeliveryMethod.HAND_DELIVERY,
                                    FIXED_INSTANT
                            ))
                            .build()
            );

            assertSuccess(
                    deliverOrderUseCase.execute(
                            new DeliverOrderCommand(
                                    orderId,
                                    DeliveryMethod.HAND_DELIVERY,
                                    "Nouvelle observation"
                            )
                    )
            );

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut REJECTED")
    class WhenOrderIsAlreadyRejected {

        @Test
        @DisplayName("La livraison échoue avec une erreur business")
        void shouldFailToDeliverOrder() {
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

            var result = deliverOrderUseCase.execute(
                    new DeliverOrderCommand(
                            orderId,
                            DeliveryMethod.HAND_DELIVERY,
                            "Remis en main propre"
                    )
            );

            assertFailure(
                    result,
                    FailureType.BUSINESS_RULE,
                    OrderErrorCode.ORDER_HAS_BEEN_REJECTED
            );
        }

        @Test
        @DisplayName("Le statut n'est pas modifié et aucun événement n'est publié")
        void shouldNotChangeStatusNorDispatchEvent() {
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

            deliverOrderUseCase.execute(
                    new DeliverOrderCommand(
                            orderId,
                            DeliveryMethod.HAND_DELIVERY,
                            "Remis en main propre"
                    )
            );

            var persistedOrder = inMemoryOrderLifecycleRepository
                    .findById(orderId)
                    .orElseThrow();

            assertThat(persistedOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Rejected.class);

            assertThat(persistedOrder.orderStatus().type())
                    .isEqualTo(OrderState.REJECTED);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }
}