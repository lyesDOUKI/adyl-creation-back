package ld.application.tests.read;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.OrderIntegrationTest;
import ld.application.infra.db.converter.OrderStatusConverter;
import ld.application.read.GetOrderService;
import ld.application.response.GetOrderResponse;
import ld.application.response.OrderLineResponse;
import ld.application.response.ProductCategoryResponse;
import ld.domain.features.order.model.OrderEvent;
import ld.domain.features.order.model.OrderReference;
import ld.domain.features.order.model.OrderStatus;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.features.product.model.ProductCategory;
import ld.domain.features.product.model.ProductStatus;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.helper.test.ResultTestSupport;
import ld.standard.lib.validation.FailureType;
import ld.standard.lib.validation.Result;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

import static ld.application.jooq.tables.Customers.CUSTOMERS;
import static ld.application.jooq.tables.OrderDeliveryAddresses.ORDER_DELIVERY_ADDRESSES;
import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.Orders.ORDERS;
import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;

@OrderIntegrationTest
class GetOrderServiceIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private GetOrderService getOrderService;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private Clock clock;
    @MockitoBean
    private AggregateEventDispatcher<OrderEvent> aggregateEventDispatcher;

    private final OrderStatusConverter converter = new OrderStatusConverter();

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(ORDER_DETAILS).execute();
        dsl.deleteFrom(ORDERS).execute();
        dsl.deleteFrom(ORDER_DELIVERY_ADDRESSES).execute();
        dsl.deleteFrom(CUSTOMERS).execute();
        dsl.deleteFrom(PRODUCT_PHOTOS).execute();
        dsl.deleteFrom(PRODUCT_COLORS).execute();
        dsl.deleteFrom(PRODUCTS).execute();
    }

    @Test
    void findById_returns_success_with_mapped_order_and_lines_when_order_exists() {
        UUID productId = UUID.randomUUID();

        insertProduct(
                productId,
                "T-shirt",
                ProductCategory.CLOTHING,
                BigDecimal.valueOf(19.90)
        );

        UUID customerIdentitySubject = UUID.randomUUID();
        insertCustomer(customerIdentitySubject);

        UUID orderId = UUID.randomUUID();

        insertOrder(
                orderId,
                customerIdentitySubject,
                "Livrer avant vendredi",
                BigDecimal.valueOf(35.82)
        );

        insertOrderDetail(
                orderId,
                productId,
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(19.90),
                BigDecimal.valueOf(35.82),
                "Rouge",
                BigDecimal.valueOf(0.10)
        );

        Result<GetOrderResponse> result =
                getOrderService.findById(orderId, customerIdentitySubject);

        assertThat(result.isSuccess()).isTrue();

        GetOrderResponse response =
                ResultTestSupport.extractValue(result);

        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.customerId()).isEqualTo(customerIdentitySubject);
        assertThat(response.customerMessage())
                .isEqualTo("Livrer avant vendredi");
        assertThat(response.total())
                .isEqualByComparingTo(BigDecimal.valueOf(35.82));
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.lineCount()).isEqualTo(1);
        assertThat(response.totalQuantity())
                .isEqualByComparingTo(BigDecimal.valueOf(2));

        assertThat(response.lines()).hasSize(1);

        OrderLineResponse line = response.lines().get(0);

        assertThat(line.productId()).isEqualTo(productId);
        assertThat(line.productName()).isEqualTo("T-shirt");
        assertThat(line.productCategory())
                .isEqualTo(ProductCategoryResponse.CLOTHING);
        assertThat(line.quantity())
                .isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(line.unitPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(19.90));
        assertThat(line.chosenColor()).isEqualTo("Rouge");
        assertThat(line.discountRate())
                .isEqualByComparingTo(BigDecimal.valueOf(0.10));
        assertThat(line.totalAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(35.82));
    }

    @Test
    void findById_aggregates_multiple_lines_for_the_same_order() {
        UUID productA = UUID.randomUUID();

        insertProduct(
                productA,
                "T-shirt",
                ProductCategory.CLOTHING,
                BigDecimal.valueOf(19.90)
        );

        UUID productB = UUID.randomUUID();

        insertProduct(
                productB,
                "Mug",
                ProductCategory.ACCESSORIES,
                BigDecimal.valueOf(9.90)
        );

        UUID customerIdentitySubject = UUID.randomUUID();
        insertCustomer(customerIdentitySubject);

        UUID orderId = UUID.randomUUID();

        insertOrder(
                orderId,
                customerIdentitySubject,
                null,
                BigDecimal.valueOf(29.80)
        );

        insertOrderDetail(
                orderId,
                productA,
                BigDecimal.ONE,
                BigDecimal.valueOf(19.90),
                BigDecimal.valueOf(19.90),
                "Bleu",
                BigDecimal.ZERO
        );

        insertOrderDetail(
                orderId,
                productB,
                BigDecimal.ONE,
                BigDecimal.valueOf(9.90),
                BigDecimal.valueOf(9.90),
                null,
                BigDecimal.ZERO
        );

        Result<GetOrderResponse> result =
                getOrderService.findById(orderId, customerIdentitySubject);

        assertThat(result.isSuccess()).isTrue();

        GetOrderResponse response =
                ResultTestSupport.extractValue(result);

        assertThat(response.lineCount()).isEqualTo(2);
        assertThat(response.totalQuantity())
                .isEqualByComparingTo(BigDecimal.valueOf(2));

        assertThat(response.lines())
                .extracting(OrderLineResponse::productName)
                .containsExactlyInAnyOrder("T-shirt", "Mug");
    }

    @Test
    void findById_returns_resource_not_found_when_order_does_not_exist() {
        UUID customerIdentitySubject = UUID.randomUUID();

        insertCustomer(customerIdentitySubject);

        Result<GetOrderResponse> result =
                getOrderService.findById(
                        UUID.randomUUID(),
                        customerIdentitySubject
                );

        assertThat(result.isFailure()).isTrue();

        ResultTestSupport.assertFailure(
                result,
                FailureType.RESOURCE_NOT_FOUND,
                OrderErrorCode.ORDER_NOT_FOUND
        );
    }

    @Test
    void findById_returns_resource_not_found_when_order_belongs_to_another_customer() {
        UUID productId = UUID.randomUUID();

        insertProduct(
                productId,
                "T-shirt",
                ProductCategory.CLOTHING,
                BigDecimal.valueOf(19.90)
        );

        UUID ownerIdentitySubject = UUID.randomUUID();
        insertCustomer(ownerIdentitySubject);

        UUID orderId = UUID.randomUUID();

        insertOrder(
                orderId,
                ownerIdentitySubject,
                "Pour moi",
                BigDecimal.valueOf(19.90)
        );

        insertOrderDetail(
                orderId,
                productId,
                BigDecimal.ONE,
                BigDecimal.valueOf(19.90),
                BigDecimal.valueOf(19.90),
                "Rouge",
                BigDecimal.ZERO
        );

        UUID otherCustomerIdentitySubject = UUID.randomUUID();
        insertCustomer(otherCustomerIdentitySubject);

        Result<GetOrderResponse> result =
                getOrderService.findById(
                        orderId,
                        otherCustomerIdentitySubject
                );

        assertThat(result.isFailure()).isTrue();

        ResultTestSupport.assertFailure(
                result,
                FailureType.RESOURCE_NOT_FOUND,
                OrderErrorCode.ORDER_NOT_FOUND
        );
    }

    @Test
    void findAll_maps_every_order_and_preserves_paging_metadata() {
        UUID productId = UUID.randomUUID();

        insertProduct(
                productId,
                "T-shirt",
                ProductCategory.CLOTHING,
                BigDecimal.valueOf(19.90)
        );

        UUID customerIdentitySubject = UUID.randomUUID();
        insertCustomer(customerIdentitySubject);

        UUID orderA = UUID.randomUUID();

        insertOrder(
                orderA,
                customerIdentitySubject,
                "A",
                BigDecimal.valueOf(19.90)
        );

        insertOrderDetail(
                orderA,
                productId,
                BigDecimal.ONE,
                BigDecimal.valueOf(19.90),
                BigDecimal.valueOf(19.90),
                "Rouge",
                BigDecimal.ZERO
        );

        UUID orderB = UUID.randomUUID();

        insertOrder(
                orderB,
                customerIdentitySubject,
                "B",
                BigDecimal.valueOf(39.80)
        );

        insertOrderDetail(
                orderB,
                productId,
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(19.90),
                BigDecimal.valueOf(39.80),
                "Bleu",
                BigDecimal.ZERO
        );

        Page<GetOrderResponse> page =
                getOrderService.findAll(
                        PageRequest.of(0, 10),
                        customerIdentitySubject
                );

        assertThat(page.getTotalElements()).isEqualTo(2);

        assertThat(page.getContent())
                .extracting(GetOrderResponse::customerMessage)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void findAll_only_returns_orders_belonging_to_the_requesting_customer() {
        UUID productId = UUID.randomUUID();

        insertProduct(
                productId,
                "T-shirt",
                ProductCategory.CLOTHING,
                BigDecimal.valueOf(19.90)
        );

        UUID customerIdentitySubject = UUID.randomUUID();
        insertCustomer(customerIdentitySubject);

        UUID otherCustomerIdentitySubject = UUID.randomUUID();
        insertCustomer(otherCustomerIdentitySubject);

        UUID myOrder = UUID.randomUUID();

        insertOrder(
                myOrder,
                customerIdentitySubject,
                "Ma commande",
                BigDecimal.valueOf(19.90)
        );

        insertOrderDetail(
                myOrder,
                productId,
                BigDecimal.ONE,
                BigDecimal.valueOf(19.90),
                BigDecimal.valueOf(19.90),
                "Rouge",
                BigDecimal.ZERO
        );

        UUID otherOrder = UUID.randomUUID();

        insertOrder(
                otherOrder,
                otherCustomerIdentitySubject,
                "Commande d'un autre",
                BigDecimal.valueOf(9.90)
        );

        insertOrderDetail(
                otherOrder,
                productId,
                BigDecimal.ONE,
                BigDecimal.valueOf(9.90),
                BigDecimal.valueOf(9.90),
                "Bleu",
                BigDecimal.ZERO
        );

        Page<GetOrderResponse> page =
                getOrderService.findAll(
                        PageRequest.of(0, 10),
                        customerIdentitySubject
                );

        assertThat(page.getTotalElements()).isEqualTo(1);

        assertThat(page.getContent())
                .extracting(GetOrderResponse::orderId)
                .containsExactly(myOrder);
    }

    @Test
    void findAll_returns_empty_page_when_no_orders_exist() {
        UUID customerIdentitySubject = UUID.randomUUID();

        insertCustomer(customerIdentitySubject);

        Page<GetOrderResponse> page =
                getOrderService.findAll(
                        PageRequest.of(0, 10),
                        customerIdentitySubject
                );

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    private void insertProduct(
            UUID productId,
            String name,
            ProductCategory category,
            BigDecimal price
    ) {
        dsl.insertInto(PRODUCTS)
                .columns(
                        PRODUCTS.ID,
                        PRODUCTS.NAME,
                        PRODUCTS.UNIT_PRICE,
                        PRODUCTS.STATUS,
                        PRODUCTS.CATEGORY
                )
                .values(
                        productId,
                        name,
                        price,
                        String.valueOf(ProductStatus.AVAILABLE),
                        String.valueOf(category)
                )
                .execute();
    }

    private void insertCustomer(UUID identitySubject) {
        dsl.insertInto(
                        CUSTOMERS,
                        CUSTOMERS.ID,
                        CUSTOMERS.IDENTITY_SUBJECT,
                        CUSTOMERS.EMAIL,
                        CUSTOMERS.PHONE
                )
                .values(
                        UUID.randomUUID(),
                        identitySubject,
                        "jean.dupont+" + identitySubject + "@test.com",
                        "0600000000"
                )
                .execute();
    }

    private void insertOrder(
            UUID orderId,
            UUID customerIdentitySubject,
            String customerMessage,
            BigDecimal total
    ) {
        UUID deliveryAddressId = UUID.randomUUID();

        insertDeliveryAddress(deliveryAddressId, orderId);

        dsl.insertInto(ORDERS)
                .columns(
                        ORDERS.ID,
                        ORDERS.CUSTOMER_IDENTITY_SUBJECT,
                        ORDERS.ORDER_REFERENCE,
                        ORDERS.DELIVERY_ADDRESS_ID,
                        ORDERS.CUSTOMER_MESSAGE,
                        ORDERS.TOTAL,
                        ORDERS.STATUS_TYPE,
                        ORDERS.STATUS_DATA
                )
                .values(
                        orderId,
                        customerIdentitySubject,
                        OrderReference.generate(clock).value(),
                        deliveryAddressId,
                        customerMessage,
                        total,
                        "PENDING",
                        JSON.json(
                                converter.convertToDatabaseColumn(
                                        OrderStatus.PENDING
                                )
                        )
                )
                .execute();
    }

    private void insertDeliveryAddress(
            UUID deliveryAddressId,
            UUID orderId
    ) {
        dsl.insertInto(ORDER_DELIVERY_ADDRESSES)
                .columns(
                        ORDER_DELIVERY_ADDRESSES.ID,
                        ORDER_DELIVERY_ADDRESSES.ADDRESS,
                        ORDER_DELIVERY_ADDRESSES.CITY
                )
                .values(
                        deliveryAddressId,
                        "10 rue de Paris",
                        "Avignon"
                )
                .execute();
    }

    private void insertOrderDetail(
            UUID orderId,
            UUID productId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            String chosenColor,
            BigDecimal discountRate
    ) {
        dsl.insertInto(
                        ORDER_DETAILS,
                        ORDER_DETAILS.ID,
                        ORDER_DETAILS.ORDER_ID,
                        ORDER_DETAILS.PRODUCT_ID,
                        ORDER_DETAILS.QUANTITY,
                        ORDER_DETAILS.UNIT_PRICE,
                        ORDER_DETAILS.TOTAL_AMOUNT,
                        ORDER_DETAILS.CHOSEN_COLOR,
                        ORDER_DETAILS.DISCOUNT_RATE
                )
                .values(
                        UUID.randomUUID(),
                        orderId,
                        productId,
                        quantity,
                        unitPrice,
                        totalAmount,
                        chosenColor,
                        discountRate
                )
                .execute();
    }
}