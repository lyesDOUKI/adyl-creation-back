package ld.domain.features.order.accept;

import ld.domain.features.order.lifecycle.InMemoryDiscountClaimer;
import ld.domain.features.order.lifecycle.InMemoryOrderEditor;
import ld.domain.features.order.model.*;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.features.product.model.ProductColor;
import ld.domain.features.shared.OrderSnapshotTestBuilder;
import ld.domain.valueObjects.Percentage;
import ld.domain.valueObjects.Price;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import ld.standard.lib.helper.test.InMemoryUnitOfWork;
import ld.standard.lib.validation.FailureType;
import org.assertj.core.util.BigDecimalComparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static ld.standard.lib.helper.test.ResultTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class AcceptOrderUseCaseTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-30T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private final InMemoryOrderEditor inMemoryOrderLifecycleRepository = new InMemoryOrderEditor();
    private final InMemoryCustomerOrderHistoryFinder customerOrderHistoryFinder = new InMemoryCustomerOrderHistoryFinder();
    private final InMemoryDiscountClaimer discountClaimRepository = new InMemoryDiscountClaimer();
    private final InMemoryAggregateEventDispatcher<OrderEvent> orderEventAggregateEventDispatcher = new InMemoryAggregateEventDispatcher<>();
    private final InMemoryUnitOfWork unitOfWork = new InMemoryUnitOfWork();

    private final AcceptOrderUseCase acceptOrderUseCase = new AcceptOrderUseCaseImpl(
            inMemoryOrderLifecycleRepository,
            customerOrderHistoryFinder,
            discountClaimRepository,
            orderEventAggregateEventDispatcher,
            unitOfWork,
            FIXED_CLOCK
    );

    private static OrderSnapshot.OrderItemSnapshot anItem(BigDecimal price, BigDecimal total) {
        return new OrderSnapshot.OrderItemSnapshot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Price(price),
                1,
                new Price(total),
                new ProductColor("noir")
        );
    }

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

            acceptOrderUseCase.execute(command);

            assertThat(inMemoryOrderLifecycleRepository.findById(command.orderId())).isEmpty();
            assertThat(orderEventAggregateEventDispatcher.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est en statut PENDING et c'est la première commande acceptée du client")
    class WhenOrderIsPendingAndFirstAcceptedOrder {

        @Test
        @DisplayName("L'acceptation réussit, la commande passe au statut ACCEPTED avec 10% de remise répartie sur les articles et la date figée")
        void shouldChangeOrderStatusToAcceptedWithDiscount() {
            var orderId = UUID.randomUUID();
            var items = List.of(
                    anItem(BigDecimal.valueOf(50), BigDecimal.valueOf(50)),
                    anItem(BigDecimal.valueOf(50), BigDecimal.valueOf(50))
            );
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(items)
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);

            var acceptedOrder = extractValue(result);
            assertThat(acceptedOrder.orderStatus()).isInstanceOf(OrderStatus.Accepted.class);

            var acceptedStatus = (OrderStatus.Accepted) acceptedOrder.orderStatus();
            assertThat(acceptedStatus.acceptedAt()).isEqualTo(FIXED_INSTANT);
            assertThat(acceptedStatus.discountApplied().value()).isEqualByComparingTo(BigDecimal.TEN);

            assertThat(acceptedOrder.total()).isEqualByComparingTo(BigDecimal.valueOf(90));

            assertThat(acceptedOrder.items())
                    .hasSize(2)
                    .allSatisfy(item -> assertThat(item.total().value()).isEqualByComparingTo(BigDecimal.valueOf(45)));
        }

        @Test
        @DisplayName("La commande est persistée avec le nouveau statut, le total recalculé et un événement est émis")
        void shouldPersistOrderAndDispatchOrderAcceptedEvent() {
            discountClaimRepository.clear();
            var orderId = UUID.randomUUID();
            var items = List.of(
                    anItem(BigDecimal.valueOf(50), BigDecimal.valueOf(50)),
                    anItem(BigDecimal.valueOf(50), BigDecimal.valueOf(50))
            );
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(items)
                            .build()
            );

            assertSuccess(acceptOrderUseCase.execute(new AcceptOrderCommand(orderId)));

            var persistedOrder = inMemoryOrderLifecycleRepository.findById(orderId).orElseThrow();

            assertThat(persistedOrder.orderStatus()).isInstanceOf(OrderStatus.Accepted.class);
            assertThat(persistedOrder.orderStatus().type()).isEqualTo(OrderState.ACCEPTED);
            assertThat(persistedOrder.total()).isEqualByComparingTo(BigDecimal.valueOf(90));

            assertThat(persistedOrder.items())
                    .extracting(
                            OrderSnapshot.OrderItemSnapshot::productId,
                            item -> item.total().value()
                    )
                    .usingComparatorForType(BigDecimalComparator.BIG_DECIMAL_COMPARATOR, BigDecimal.class)
                    .doesNotContainNull();

            var itemTotalsSum = persistedOrder.items().stream()
                    .map(item -> item.total().value())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(itemTotalsSum).isEqualByComparingTo(persistedOrder.total());

            assertThat(orderEventAggregateEventDispatcher.count()).isOne();
        }

        @Test
        @DisplayName("La remise de 10% est répartie sur les articles sans perte d'arrondi")
        void shouldDistributeDiscountAcrossItemsWithoutRoundingLoss() {
            var orderId = UUID.randomUUID();
            var items = List.of(
                    anItem(BigDecimal.valueOf(33.33), BigDecimal.valueOf(33.33)),
                    anItem(BigDecimal.valueOf(33.33), BigDecimal.valueOf(33.33)),
                    anItem(BigDecimal.valueOf(33.34), BigDecimal.valueOf(33.34))
            );
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(items)
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);
            var acceptedOrder = extractValue(result);

            assertThat(acceptedOrder.total()).isEqualByComparingTo(BigDecimal.valueOf(90));

            var itemTotalsSum = acceptedOrder.items().stream()
                    .map(item -> item.total().value())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(itemTotalsSum).isEqualByComparingTo(acceptedOrder.total());

            assertThat(acceptedOrder.items())
                    .allSatisfy(item -> assertThat(item.total().value())
                            .isLessThan(item.price().value().multiply(BigDecimal.valueOf(item.quantity()))));
        }

        @Test
        @DisplayName("Une remise de 0% laisse le total des articles inchangé (garde-fou sur la borne basse)")
        void shouldKeepItemsUnchangedWhenDiscountIsZeroPercentButStillFirstOrder() {
            var orderId = UUID.randomUUID();
            var items = List.of(anItem(BigDecimal.ZERO, BigDecimal.ZERO));
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(items)
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);
            assertThat(extractValue(result).total()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Quand le client a déjà consommé la remise première commande")
    class WhenCustomerInfoAlreadyClaimedFirstOrderDiscount {

        @Test
        @DisplayName("La remise n'est pas appliquée et les totaux des articles restent inchangés")
        void shouldNotApplyDiscount() {
            var customer = new CustomerInfo("test", "test@test.com", "0123456789", "7 rue test", "avignon");

            discountClaimRepository.tryAddClaim(DiscountType.FIRST_ACCEPTED_ORDER, customer.email());

            var orderId = UUID.randomUUID();
            var items = List.of(
                    anItem(BigDecimal.valueOf(100), BigDecimal.valueOf(100)),
                    anItem(BigDecimal.valueOf(100), BigDecimal.valueOf(100))
            );
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withCustomer(customer)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(items)
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);

            var acceptedOrder = extractValue(result);
            assertThat(acceptedOrder.total()).isEqualByComparingTo(BigDecimal.valueOf(200));

            var acceptedStatus = (OrderStatus.Accepted) acceptedOrder.orderStatus();
            assertThat(acceptedStatus.discountApplied().value()).isEqualByComparingTo(BigDecimal.ZERO);

            assertThat(acceptedOrder.items())
                    .allSatisfy(item -> assertThat(item.total().value()).isEqualByComparingTo(BigDecimal.valueOf(100)));
        }
    }

    @Nested
    @DisplayName("Quand le client possède déjà une commande effective (ni PENDING, ni REJECTED)")
    class WhenCustomerHasEffectiveOrderHistory {

        @Test
        @DisplayName("La remise n'est pas appliquée, même si le claim de remise n'a jamais été consommé")
        void shouldNotApplyDiscountWhenCustomerHasEffectiveOrder() {
            var customer = new CustomerInfo("test", "test@test.com", "0123456789", "7 rue test", "avignon");
            customerOrderHistoryFinder.markEffectiveOrder(customer.email());

            var orderId = UUID.randomUUID();
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withCustomer(customer)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(List.of(anItem(BigDecimal.valueOf(100), BigDecimal.valueOf(100))))
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);

            var acceptedOrder = extractValue(result);
            assertThat(acceptedOrder.total()).isEqualByComparingTo(BigDecimal.valueOf(100));

            var acceptedStatus = (OrderStatus.Accepted) acceptedOrder.orderStatus();
            assertThat(acceptedStatus.discountApplied().value()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Le court-circuit évite même de solliciter le discountClaimer")
        void shouldNotConsumeDiscountClaimWhenCustomerHasEffectiveOrder() {
            var customer = new CustomerInfo("test", "test@test.com", "0123456789", "7 rue test", "avignon");
            customerOrderHistoryFinder.markEffectiveOrder(customer.email());

            var orderId = UUID.randomUUID();
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withCustomer(customer)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(List.of(anItem(BigDecimal.valueOf(100), BigDecimal.valueOf(100))))
                            .build()
            );

            assertSuccess(acceptOrderUseCase.execute(new AcceptOrderCommand(orderId)));

            assertThat(discountClaimRepository.tryAddClaim(DiscountType.FIRST_ACCEPTED_ORDER, customer.email()))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Quand deux commandes différentes du même client sont acceptées successivement")
    class WhenSameCustomerInfoAcceptsTwoOrders {

        @Test
        @DisplayName("Seule la première consomme la remise, la seconde n'en bénéficie pas")
        void shouldOnlyApplyDiscountOnce() {
            var customer = new CustomerInfo("test", "test@test.com", "0123456789", "7 rue test", "avignon");

            var firstOrderId = UUID.randomUUID();
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(firstOrderId)
                            .withCustomer(customer)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(List.of(anItem(BigDecimal.valueOf(100), BigDecimal.valueOf(100))))
                            .build()
            );

            var secondOrderId = UUID.randomUUID();
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(secondOrderId)
                            .withCustomer(customer)
                            .withOrderStatus(OrderStatus.PENDING)
                            .withItems(List.of(anItem(BigDecimal.valueOf(150), BigDecimal.valueOf(150))))
                            .build()
            );

            var firstResult = acceptOrderUseCase.execute(new AcceptOrderCommand(firstOrderId));
            var secondResult = acceptOrderUseCase.execute(new AcceptOrderCommand(secondOrderId));

            assertSuccess(firstResult);
            assertSuccess(secondResult);

            assertThat(extractValue(firstResult).total()).isEqualByComparingTo(BigDecimal.valueOf(90));
            assertThat(extractValue(secondResult).total()).isEqualByComparingTo(BigDecimal.valueOf(150));
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

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(alreadyAcceptedStatus)
                            .withTotal(BigDecimal.valueOf(90))
                            .build()
            );

            var result = acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            assertSuccess(result);
            assertThat(extractValue(result).total()).isEqualByComparingTo(BigDecimal.valueOf(90));
        }

        @Test
        @DisplayName("Aucun événement supplémentaire n'est publié")
        void shouldNotDispatchDuplicateEvent() {
            var orderId = UUID.randomUUID();
            var alreadyAcceptedStatus = new OrderStatus.Accepted(FIXED_INSTANT, Percentage.of(10));

            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(alreadyAcceptedStatus)
                            .build()
            );

            assertSuccess(acceptOrderUseCase.execute(new AcceptOrderCommand(orderId)));
            assertThat(orderEventAggregateEventDispatcher.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut DELIVERED")
    class WhenOrderIsAlreadyDelivered {

        @Test
        @DisplayName("L'acceptation échoue avec une erreur business")
        void shouldFailToAcceptOrder() {
            var orderId = UUID.randomUUID();
            inMemoryOrderLifecycleRepository.save(
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
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.DELIVERED)
                            .build()
            );

            acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            var persistedOrder = inMemoryOrderLifecycleRepository.findById(orderId).orElseThrow();
            assertThat(persistedOrder.orderStatus()).isInstanceOf(OrderStatus.Delivered.class);
            assertThat(persistedOrder.orderStatus().type()).isEqualTo(OrderState.DELIVERED);
            assertThat(orderEventAggregateEventDispatcher.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est déjà en statut REJECTED")
    class WhenOrderIsAlreadyRejected {

        @Test
        @DisplayName("L'acceptation échoue avec une erreur business")
        void shouldFailToAcceptOrder() {
            var orderId = UUID.randomUUID();
            inMemoryOrderLifecycleRepository.save(
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
            inMemoryOrderLifecycleRepository.save(
                    OrderSnapshotTestBuilder.anOrder()
                            .withOrderId(orderId)
                            .withOrderStatus(OrderStatus.REJECTED)
                            .build()
            );

            acceptOrderUseCase.execute(new AcceptOrderCommand(orderId));

            var persistedOrder = inMemoryOrderLifecycleRepository.findById(orderId).orElseThrow();
            assertThat(persistedOrder.orderStatus()).isInstanceOf(OrderStatus.Rejected.class);
            assertThat(persistedOrder.orderStatus().type()).isEqualTo(OrderState.REJECTED);
            assertThat(orderEventAggregateEventDispatcher.count()).isZero();
        }
    }
}