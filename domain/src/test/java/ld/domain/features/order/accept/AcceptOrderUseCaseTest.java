package ld.domain.features.order.accept;


import ld.domain.features.order.lifecycle.DiscountClaimRepository;
import ld.domain.features.order.lifecycle.InMemoryDiscountClaimRepository;
import ld.domain.features.order.lifecycle.InMemoryOrderLifecycleRepository;
import ld.domain.features.order.lifecycle.OrderLifecycleRepository;
import ld.domain.features.order.model.Customer;
import ld.domain.features.order.model.DiscountType;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderStatus;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.features.shared.OrderSnapshotTestBuilder;
import ld.domain.valueObjects.Percentage;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.standard.lib.helper.test.InMemoryUnitOfWork;
import ld.standard.lib.validation.FailureType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static ld.standard.lib.helper.test.ResultTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class AcceptOrderUseCaseTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-30T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private final OrderLifecycleRepository orderLifecycleRepository = new InMemoryOrderLifecycleRepository();
    private final DiscountClaimRepository discountClaimRepository = new InMemoryDiscountClaimRepository();
    private final InMemoryAggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();
    private final UnitOfWork unitOfWork = new InMemoryUnitOfWork();

    private final AcceptOrderUseCase acceptOrderUseCase = new AcceptOrderUseCaseImpl(
            orderLifecycleRepository,
            discountClaimRepository,
            orderEventAggregateEventDispatcher,
            unitOfWork,
            FIXED_CLOCK
    );

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
    @DisplayName("Quand la commande est en statut PENDING et c'est la première commande acceptée du client")
    class WhenOrderIsPendingAndFirstAcceptedOrder {

        @Test
        @DisplayName("L'acceptation réussit, la commande passe au statut ACCEPTED avec 10% de remise et la date figée")
        void shouldChangeOrderStatusToAcceptedWithDiscount() {
            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withTotal(BigDecimal.valueOf(100))
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);

            var acceptedOrder = extractValue(result);
            assertThat(acceptedOrder.orderStatus())
                    .isInstanceOf(OrderStatus.Accepted.class);

            var acceptedStatus = (OrderStatus.Accepted) acceptedOrder.orderStatus();
            assertThat(acceptedStatus.acceptedAt())
                    .isEqualTo(FIXED_INSTANT);
            assertThat(acceptedStatus.discountApplied().value())
                    .isEqualByComparingTo(BigDecimal.TEN);

            assertThat(acceptedOrder.total())
                    .isEqualByComparingTo(BigDecimal.valueOf(90));
        }

        @Test
        @DisplayName("La commande est persistée avec le nouveau statut et un événement est émis")
        void shouldPersistOrderAndDispatchOrderAcceptedEvent() {
            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withTotal(BigDecimal.valueOf(100))
                            .build()
            );

            assertSuccess(acceptOrderUseCase.execute(new AcceptOrderCommand(orderId)));

            var persistedOrder = orderLifecycleRepository.findById(orderId);
            assertThat(persistedOrder)
                    .isPresent();
            assertThat(persistedOrder.get().orderStatus())
                    .isInstanceOf(OrderStatus.Accepted.class);
            assertThat(persistedOrder.get().total())
                    .isEqualByComparingTo(BigDecimal.valueOf(90));

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isOne();
        }
    }

    @Nested
    @DisplayName("Quand le client a déjà consommé la remise première commande")
    class WhenCustomerAlreadyClaimedFirstOrderDiscount {

        @Test
        @DisplayName("La remise n'est pas appliquée sur la nouvelle commande acceptée")
        void shouldNotApplyDiscount() {
            var customer = new Customer("test", "test@test.com", "0123456789", "7 rue test", "avignon");

            // Simule une remise déjà consommée par ce client
            discountClaimRepository.tryClaim(DiscountType.FIRST_ACCEPTED_ORDER, customer.email());

            var orderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withCustomer(customer)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withTotal(BigDecimal.valueOf(200))
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);

            var acceptedOrder = extractValue(result);
            assertThat(acceptedOrder.total())
                    .isEqualByComparingTo(BigDecimal.valueOf(200));

            var acceptedStatus = (OrderStatus.Accepted) acceptedOrder.orderStatus();
            assertThat(acceptedStatus.discountApplied().value())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Quand deux commandes différentes du même client sont acceptées successivement")
    class WhenSameCustomerAcceptsTwoOrders {

        @Test
        @DisplayName("Seule la première consomme la remise, la seconde n'en bénéficie pas")
        void shouldOnlyApplyDiscountOnce() {
            var customer = new Customer("test", "test@test.com", "0123456789", "7 rue test", "avignon");

            var firstOrderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(firstOrderId)
                            .withCustomer(customer)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withTotal(BigDecimal.valueOf(100))
                            .build()
            );

            var secondOrderId = UUID.randomUUID();
            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(secondOrderId)
                            .withCustomer(customer)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withTotal(BigDecimal.valueOf(150))
                            .build()
            );

            var firstResult = acceptOrderUseCase.execute(new AcceptOrderCommand(firstOrderId));
            var secondResult = acceptOrderUseCase.execute(new AcceptOrderCommand(secondOrderId));

            assertSuccess(firstResult);
            assertSuccess(secondResult);

            assertThat(extractValue(firstResult).total())
                    .isEqualByComparingTo(BigDecimal.valueOf(90));
            assertThat(extractValue(secondResult).total())
                    .isEqualByComparingTo(BigDecimal.valueOf(150));
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut ACCEPTED")
    class WhenOrderIsAlreadyAccepted {

        @Test
        @DisplayName("L'acceptation réussit sans rien changer (idempotence)")
        void shouldReturnSuccessWithoutChangingAnything() {
            var orderId = UUID.randomUUID();
            var alreadyAcceptedStatus = new OrderStatus.Accepted(FIXED_INSTANT, Percentage.of(10));

            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(alreadyAcceptedStatus)
                            .withTotal(BigDecimal.valueOf(90))
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);
            assertThat(extractValue(result).total())
                    .isEqualByComparingTo(BigDecimal.valueOf(90));
        }

        @Test
        @DisplayName("Aucun événement supplémentaire n'est publié")
        void shouldNotDispatchDuplicateEvent() {
            var orderId = UUID.randomUUID();
            var alreadyAcceptedStatus = new OrderStatus.Accepted(FIXED_INSTANT, Percentage.of(10));

            orderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(alreadyAcceptedStatus)
                            .build()
            );

            assertSuccess(acceptOrderUseCase.execute(new AcceptOrderCommand(orderId)));

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
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
                    .isEqualTo(OrderStatus.DELIVERED);

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
                    .isEqualTo(OrderStatus.REJECTED);

            assertThat(orderEventAggregateEventDispatcher.count())
                    .isZero();
        }
    }
}